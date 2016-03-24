import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.text.DecimalFormat;
import java.util.Scanner;

public class cdht_ex {

    private static final int BASE_PORT = 50000;
    private static final String IP = "127.0.0.1";

    private static final String DATA_PING_REQUEST = "ping";
    private static final String DATA_PING_RESPONSE = "ping response";
    private static final String DATA_FILE_REQUEST = "file request";
    private static final String DATA_FILE_RESPONSE = "file response";
    private static final String DATA_QUIT = "quit";
    private static final String DATA_CLOSE = "close";
    private static final String DATA_SUCCESSORS_REQUEST = "successors request";
    private static final String DATA_SUCCESSORS_RESPONSE = "successors response";
    private static final String DATA_PREDECESSOR = "predecessor";
    private static final String DATA_MISSING = "missing";

    private static final String COMMOND_REQUEST_FILE = "request";
    private static final String COMMOND_QUIT = "quit";

    private static final int PRINT_PING_REQUEST = 0x0001;
    private static final int PRINT_PING_RESPONSE = 0x0002;
    private static final int PRINT_FILENAME_OUT_OF_BOUNDARY = 0x0003;
    private static final int PRINT_FILENAME_INVALID = 0x0004;
    private static final int PRINT_FILE_REQUEST_SEND = 0x0005;
    private static final int PRINT_FILE_REQUEST_FORWARD = 0x0006;
    private static final int PRINT_FILE_REQUEST_IS_NOT_HERE = 0x0007;
    private static final int PRINT_FILE_REQUEST_IS_HERE = 0x0008;
    private static final int PRINT_FILE_REQUEST_REPONSE = 0x0009;
    private static final int PRINT_FILE_REQINT_SEND_FILE = 0x000A;
    private static final int PRINT_QUIT_NOTIFICATION = 0x000B;
    private static final int PRINT_QUIT_CHANGE = 0x000C;
    private static final int PRINT_FORCE_QUIT = 0x000D;

    private int peerId;
    private int firstSuccessorId;
    private int secondSuccessorId;

    private int predecessorsId1 = -1;
    private int predecessorsId2 = -2;
    private boolean isResponseFromPredecessors1 = false;
    private boolean isResponseFromPredecessors2 = false;
    private InetAddress address;
    private int pingTimeout = 10;
    private int pingPeriodRepeatTimes = 200;
    private int pingPeriodUnit = 100;
    public PingRequestThread pingRequest1;
    public PingRequestThread pingRequest2;
    private DatagramSocket udpSocket;
    public boolean isQuit = false;
    private boolean isBreakPingSleep1 = false;
    private boolean isBreakPingSleep2 = false;

    private int[] aliveSuccessorInfo = new int[3];
    DecimalFormat df = new DecimalFormat("0000");


    public static void main(String[] args) throws InterruptedException {

        cdht_ex myCdht = new cdht_ex();
        if (!myCdht.initArgs(args)) {
            return;
        }
        try {
            myCdht.init();
        } catch (IOException e) {
            System.out.println("init failed!!!! Try again please.");
        }
        Scanner s = new Scanner(System.in);
        while (true) {
            String commond = s.nextLine();
            try {
                myCdht.inputCommond(commond);
            } catch (Exception e) {
                System.out.println("input incorrect. Try ooagain please.");
            }
            if (commond.equals("quit")) {
                System.out.println("Quiting...");
                while (!myCdht.isQuit) {
//                    System.out.println("waiting for quit");
                    Thread.sleep(100);
                }
                break;
            }
        }
        myCdht.isQuit = false;
        while (!myCdht.isQuit) {
            myCdht.stop();
            Thread.sleep(100);
        }
        return;
    }

    public void inputCommond(String commond) throws Exception {
        int fileRequestPos = commond.indexOf(COMMOND_REQUEST_FILE);
        int quitPos = commond.indexOf(COMMOND_QUIT);
        if (fileRequestPos != -1) {
            String fileNameStr = commond.subSequence(fileRequestPos + COMMOND_REQUEST_FILE.length(), commond.length()).toString().trim();
            if(fileNameStr.length() != 4){
                System.out.println("argument should be a 4 digits number.");
            }else{
                int fileNameInt = Integer.parseInt(fileNameStr);
                if (fileNameInt < 1 || fileNameInt > 9999) {
                    printInfo(PRINT_FILENAME_OUT_OF_BOUNDARY, new Object[]{fileNameInt});
                    return;
                } else {
                    try {
                        sendFileRequest(getClosestPort(fileNameInt), fileNameInt, peerId);
                        printInfo(PRINT_FILE_REQUEST_SEND, new Object[]{fileNameInt});
                    } catch (IOException ie) {
                        System.out.println(ie.toString());
                    }
                }
            }
        } else if (quitPos != -1) {
            try {
                sendQuit(predecessorsId1);
                sendQuit(predecessorsId2);
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }

    }

    /**
     * initialize the peerId, firstSuccessorId, secondSuccessId by the arguments.
     *
     * @param args arguments
     * @return whether the initialization runs correctly.
     * True correctly.
     * False incorrectly.
     */
    public boolean initArgs(String[] args) {
        if (args.length != 3) {
            System.out.println("wrong number of arguments");
            return false;
        }
        peerId = string2Int(args[0]);
        firstSuccessorId = string2Int(args[1]);
        secondSuccessorId = string2Int(args[2]);
        if (peerId < 0 || peerId > 255) {
            System.out.println("argument: peerId " + peerId + " is invalid");
            return false;
        }
        if (firstSuccessorId < 0 || firstSuccessorId > 255) {
            System.out.println("argument: firstSuccessorId " + firstSuccessorId + " is invalid");
            return false;
        }
        if (secondSuccessorId < 0 || secondSuccessorId > 255) {
            System.out.println("argument: secondSuccessorId " + secondSuccessorId + " is invalid");
            return false;
        }
        return true;

    }

    private void init() throws IOException {
        address = InetAddress.getByName(IP);

        udpSocket = new DatagramSocket(BASE_PORT + peerId);
        initPingRequest(udpSocket);
        UdpListener udpListener = new UdpListener(udpSocket);
        udpListener.start();


        ServerSocket tcpSocket = new ServerSocket(BASE_PORT + peerId);
        TcpListener tcpListener = new TcpListener(tcpSocket);
        tcpListener.start();
    }

    public void initPingRequest(DatagramSocket socket) throws SocketException {
        pingRequest1 = new PingRequestThread(socket, address, 1);
        pingRequest2 = new PingRequestThread(socket, address, 2);
        pingRequest1.start();
        pingRequest2.start();
    }

    private void sendClose(int port) throws IOException {
        Socket clientSocket = new Socket(address, port);
        String sentence = DATA_CLOSE + peerId;

        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        outToServer.writeBytes(sentence + '\n');
        clientSocket.close();
    }

    private void sendQuit(int port) throws IOException {
        //create socket which connects to server
        Socket clientSocket = new Socket(address, port);

        // get input from keyboard
        String sentence = DATA_QUIT + peerId + "," + firstSuccessorId + "," + secondSuccessorId;

        // write to server
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        outToServer.writeBytes(sentence + '\n');

        clientSocket.close();
    }

    private void sendFile(int port, int fileName) throws IOException {
        //create socket which connects to server
        Socket clientSocket = new Socket(address, BASE_PORT + port);

        // get input from keyboard
        String sentence = DATA_FILE_RESPONSE + fileName + "," + peerId;

        // write to server
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        outToServer.writeBytes(sentence + '\n');

        clientSocket.close();
    }

    private void sendFileRequest(int requestPortDestination, int fileName, int requestPortSource) throws IOException {
        //create socket which connects to server
        Socket clientSocket = new Socket(address, BASE_PORT + requestPortDestination);

        // get input from keyboard
        String sentence = DATA_FILE_REQUEST + fileName + "," + requestPortSource;

        // write to server
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        outToServer.writeBytes(sentence + '\n');

        clientSocket.close();
    }

    private void sendPredecessors(int port, int pre1, int pre2) throws IOException {
        Socket clientSocket = new Socket(address, BASE_PORT + port);

        String sentence = DATA_PREDECESSOR + pre1 + "," + pre2;

        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        outToServer.writeBytes(sentence + '\n');

        clientSocket.close();
    }

    private void sendSuccessorsRequest(int port) throws IOException {
        Socket clientSocket = new Socket(address, port);
        String sentence = DATA_SUCCESSORS_REQUEST + peerId;

        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        outToServer.writeBytes(sentence + '\n');
        clientSocket.close();
    }

    private void sendSuccessorsResponse(int port) throws IOException {
        Socket clientSocket = new Socket(address, port);
        String sentence = null;
        if (pingRequest1.timeout != PingRequestThread.TIMEOUT_FINISH_CHANGING_SUCCESSOR) {
            sentence = DATA_SUCCESSORS_RESPONSE + peerId + "," + firstSuccessorId + "," + secondSuccessorId;
        } else {
            sentence = DATA_SUCCESSORS_RESPONSE + peerId + "," + firstSuccessorId + "," + firstSuccessorId;
        }

        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        outToServer.writeBytes(sentence + '\n');
        clientSocket.close();
    }

    private void sendMissingToPredecessor(int port, int missingId, int nextId) throws IOException {
        Socket clientSocket = new Socket(address, port);
        String sentence = DATA_MISSING + peerId + "," + nextId + "," + missingId;
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        outToServer.writeBytes(sentence + '\n');
        clientSocket.close();
    }

    public void stop() {
        try {
            DatagramPacket request = new DatagramPacket(DATA_CLOSE.getBytes(), DATA_CLOSE.length(), address, BASE_PORT + peerId);
            udpSocket.send(request);

        } catch (IOException e) {
            System.out.println("during stopping process \n" + e.toString());
        }
    }

    private int getClosestPort(int fileName) {
        int hashValue = getHashValue(fileName);
        int result = -1;
        int nearerPredecessor = -1;
        if ((predecessorsId1 - BASE_PORT) > peerId && (predecessorsId2 - BASE_PORT) > peerId) {
            nearerPredecessor = Math.max(predecessorsId1, predecessorsId2) - BASE_PORT;
        } else if ((predecessorsId1 - BASE_PORT) < peerId && (predecessorsId2 - BASE_PORT) < peerId) {
            nearerPredecessor = Math.max(predecessorsId1, predecessorsId2) - BASE_PORT;
        } else {
            nearerPredecessor = Math.min(predecessorsId1, predecessorsId2) - BASE_PORT;
        }
        boolean isZeroBetween1and2 = false;
        boolean isZeroBetween2and3 = false;
        if (firstSuccessorId < peerId) {
            isZeroBetween1and2 = true;
        }
        if (secondSuccessorId < firstSuccessorId) {
            isZeroBetween2and3 = true;
        }
        if (!isZeroBetween1and2 && !isZeroBetween2and3) {
            if (hashValue > peerId) {
                if (hashValue > firstSuccessorId) {
                    if (nearerPredecessor > peerId) {
                        if (hashValue > nearerPredecessor) {
                            result = peerId;
                        } else {
                            result = secondSuccessorId;
                        }
                    } else {
                        result = secondSuccessorId;
                    }
                } else {
                    result = firstSuccessorId;
                }
            } else {
                if (nearerPredecessor < peerId) {
                    if (hashValue <= nearerPredecessor) {
                        result = secondSuccessorId;
                    } else {
                        result = peerId;
                    }
                } else {
                    result = peerId;
                }

            }
        } else if (isZeroBetween1and2) {
            if (hashValue > peerId || hashValue <= firstSuccessorId) {
                result = firstSuccessorId;
            } else {
                if (hashValue <= nearerPredecessor) {
                    result = secondSuccessorId;
                } else {
                    result = peerId;
                }
            }
        } else if (isZeroBetween2and3) {
            if (hashValue > peerId && hashValue <= firstSuccessorId) {
                result = firstSuccessorId;
            } else if (hashValue > nearerPredecessor && hashValue <= peerId) {
                result = peerId;
            } else {
                result = secondSuccessorId;
            }
        }
//        System.out.println("(" + predecessorsId1 + "/" + predecessorsId2 + ")" + nearerPredecessor + "-->" + peerId + "-->" + firstSuccessorId + "-->" + secondSuccessorId + " === " + result);

        return result;
    }

    private int getHashValue(int fileName) {
        return fileName % 256;
    }

    class PingRequestThread extends Thread {
        final public static int TIMEOUT_TIMEOUT = 0;
        final public static int TIMEOUT_RECEIVE_SUCCESSFULLY = -1;
        final public static int TIMEOUT_WAITING_FOR_CHANGING_SUCCESSOR = -2;
        final public static int TIMEOUT_START_CHANGING_SUCCESSOR = -3;
        final public static int TIMEOUT_FINISH_CHANGING_SUCCESSOR = -4;


        DatagramSocket socket;
        InetAddress requestIP;
        int portId;
        public int timeout;
        int timeoutCounter = 5;
        boolean isUpdatePing = false;

        public PingRequestThread(DatagramSocket socket, InetAddress requestIP, int portId) {
            this.socket = socket;
            this.requestIP = requestIP;
            this.portId = portId;
            this.timeout = pingTimeout;
        }

        public void run() {
            while (!isQuit) {
                int port = -1;
                try {
                    if (portId == 1) {
                        port = BASE_PORT + firstSuccessorId;
                    } else if (portId == 2) {
                        port = BASE_PORT + secondSuccessorId;
                    }

                    String requestMsg = DATA_PING_REQUEST;
                    if (isBreakPingSleep1 && portId == 1) {
                        isBreakPingSleep1 = false;
                        requestMsg = requestMsg + "0";
                    }
                    if (isBreakPingSleep2 && portId == 2) {
                        isBreakPingSleep2 = false;
                        requestMsg = requestMsg + "0";
                    }

                    DatagramPacket request = new DatagramPacket(requestMsg.getBytes(),
                            requestMsg.length(), requestIP, port);
                    socket.send(request);
                    if (timeout == TIMEOUT_FINISH_CHANGING_SUCCESSOR) {
                        isUpdatePing = true;
                    } else {
                        isUpdatePing = false;
                    }
                    timeout = pingTimeout;
//                    System.out.println("send msg to " + port + "   " + predecessorsId1 + "/" + predecessorsId2 + "   " + firstSuccessorId + "/" + secondSuccessorId);
                    for (int i = 0; i < pingPeriodRepeatTimes; i++) {
                        if (isBreakPingSleep1 && portId == 1) {
                            break;
                        }
                        if (isBreakPingSleep2 && portId == 2) {
                            break;
                        }
                        if (peerId == 1) {
                            if (timeout > TIMEOUT_TIMEOUT) {
                                timeout--;
                            } else if (timeout == TIMEOUT_TIMEOUT) {
                                System.out.println("ping " + port + "time out, " + timeoutCounter + " attempts left.");
                                timeoutCounter--;
                                if (timeoutCounter == 0) {
                                    printInfo(PRINT_FORCE_QUIT, new Object[]{port - BASE_PORT});
                                    int nearerPredecessor = -1;
                                    if ((predecessorsId1 - BASE_PORT) > peerId && (predecessorsId2 - BASE_PORT) > peerId) {
                                        nearerPredecessor = Math.max(predecessorsId1, predecessorsId2) - BASE_PORT;
                                    } else if ((predecessorsId1 - BASE_PORT) < peerId && (predecessorsId2 - BASE_PORT) < peerId) {
                                        nearerPredecessor = Math.max(predecessorsId1, predecessorsId2) - BASE_PORT;
                                    } else {
                                        nearerPredecessor = Math.min(predecessorsId1, predecessorsId2) - BASE_PORT;
                                    }
//                                    System.out.println("(" + predecessorsId1 + "/" + predecessorsId2 + ")" + nearerPredecessor + "-->" + peerId + "-->" + firstSuccessorId + "-->" + secondSuccessorId + " === " + port);
                                    sendMissingToPredecessor(nearerPredecessor + BASE_PORT, firstSuccessorId, secondSuccessorId);
                                    if (port == firstSuccessorId + BASE_PORT) {
                                        sendSuccessorsRequest(secondSuccessorId + BASE_PORT);
                                    } else if (port == secondSuccessorId + BASE_PORT) {
                                        sendSuccessorsRequest(firstSuccessorId + BASE_PORT);
                                    }
                                    timeout = TIMEOUT_WAITING_FOR_CHANGING_SUCCESSOR;
                                }
                                break;
                            } else if (timeout == TIMEOUT_RECEIVE_SUCCESSFULLY) {
                                timeoutCounter = 5;
                                if (isUpdatePing) {
                                    timeout = TIMEOUT_FINISH_CHANGING_SUCCESSOR;
                                    isUpdatePing = false;
                                }
                            } else if (timeout == TIMEOUT_WAITING_FOR_CHANGING_SUCCESSOR) {

                            } else if (timeout == TIMEOUT_START_CHANGING_SUCCESSOR) {
                                if (aliveSuccessorInfo[0] == firstSuccessorId) {
                                    secondSuccessorId = aliveSuccessorInfo[2];
                                } else if (aliveSuccessorInfo[0] == secondSuccessorId) {
                                    firstSuccessorId = secondSuccessorId;
                                    secondSuccessorId = aliveSuccessorInfo[1];
                                }
//                                System.out.println("successors of successor " + aliveSuccessorInfo[0] + "/" + aliveSuccessorInfo[1] + "/" + aliveSuccessorInfo[2]);
                                printInfo(PRINT_QUIT_CHANGE, new Object[]{firstSuccessorId, secondSuccessorId});
                                sendPredecessors(secondSuccessorId, peerId, firstSuccessorId);
                                timeout = TIMEOUT_FINISH_CHANGING_SUCCESSOR;
                                isBreakPingSleep1 = true;
                                isBreakPingSleep2 = true;
                            } else if (timeout == TIMEOUT_FINISH_CHANGING_SUCCESSOR) {
                                timeoutCounter = 5;
                            }
                        }
                        sleep(pingPeriodUnit);
                    }
                } catch (IOException | InterruptedException ie) {
                    System.out.println("can not receive the response of the destination" + port);
                }
            }

        }
    }

    class UdpListener extends Thread {
        private DatagramSocket socket;

        public UdpListener(DatagramSocket socket) {
            this.socket = socket;
        }

        public void run() {
            while (!isQuit) {
                DatagramPacket pk = new DatagramPacket(new byte[1024], 1024);
                try {
                    socket.receive(pk);
                } catch (IOException e) {
//                    System.out.println("udp listener time out");
                }
                String str = new String(pk.getData(), 0, pk.getLength());


                if (str.equals(DATA_PING_REQUEST)) {
                    int portNum = pk.getPort();

                    if (predecessorsId1 != predecessorsId2) {
                        if (portNum != predecessorsId1 && portNum != predecessorsId2) {
                            predecessorsId1 = portNum;
                            predecessorsId2 = portNum;
                        }
                    } else {
                        predecessorsId2 = portNum;
                    }

                    printInfo(PRINT_PING_REQUEST, new Object[]{portNum});
                    DatagramPacket response = new DatagramPacket(DATA_PING_RESPONSE.getBytes(), DATA_PING_RESPONSE.length(), pk.getAddress(), portNum);
                    try {
                        socket.send(response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (str.equals(DATA_PING_RESPONSE)) {
                    int portOfResponser = pk.getPort();
                    if (portOfResponser == BASE_PORT + firstSuccessorId) {
                        if (pingRequest1.timeout != PingRequestThread.TIMEOUT_FINISH_CHANGING_SUCCESSOR) {
                            pingRequest1.timeout = PingRequestThread.TIMEOUT_RECEIVE_SUCCESSFULLY;
                        }
                    } else if (portOfResponser == BASE_PORT + secondSuccessorId) {
                        if (pingRequest2.timeout != PingRequestThread.TIMEOUT_FINISH_CHANGING_SUCCESSOR) {
                            pingRequest2.timeout = PingRequestThread.TIMEOUT_RECEIVE_SUCCESSFULLY;
                        }
                    }
                    printInfo(PRINT_PING_RESPONSE, new Object[]{portOfResponser});
                } else if (str.equals(DATA_CLOSE)) {
                    isQuit = true;
                }
            }
        }
    }

    class TcpListener extends Thread {
        ServerSocket tcpSocket;

        public TcpListener(ServerSocket tcpSocket) {
            this.tcpSocket = tcpSocket;
        }

        public void run() {
            while (!isQuit) {
                try {
                    // accept connection from connection queue
                    Socket connectionSocket = tcpSocket.accept();

                    // create read stream to get input
                    BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                    String clientSentence;
                    clientSentence = inFromClient.readLine();
                    int fileRequestPos = clientSentence.indexOf(DATA_FILE_REQUEST);
                    int fileResponsePos = clientSentence.indexOf(DATA_FILE_RESPONSE);
                    int quitPos = clientSentence.indexOf(DATA_QUIT);
                    int closePos = clientSentence.indexOf(DATA_CLOSE);
                    int successorsRequestPos = clientSentence.indexOf(DATA_SUCCESSORS_REQUEST);
                    int successorsResponsePos = clientSentence.indexOf(DATA_SUCCESSORS_RESPONSE);
                    int predecessorPos = clientSentence.indexOf(DATA_PREDECESSOR);
                    int missingPos = clientSentence.indexOf(DATA_MISSING);
                    if (fileRequestPos != -1) {
                        String remanentPart = clientSentence.substring(fileRequestPos + DATA_FILE_REQUEST.length());
                        int commaPos = remanentPart.indexOf(",");
                        String fileNameStr = remanentPart.substring(0, commaPos);
                        String requestPortStr = remanentPart.substring(commaPos + 1);

                        int fileNameInt = Integer.parseInt(fileNameStr);
                        int requestPortInt = Integer.parseInt(requestPortStr);
                        int closestPort = getClosestPort(fileNameInt);
                        if (closestPort == peerId) {
                            printInfo(PRINT_FILE_REQUEST_IS_HERE, new Object[]{fileNameInt});
                            sendFile(requestPortInt, fileNameInt);
                            printInfo(PRINT_FILE_REQINT_SEND_FILE, new Object[]{requestPortInt});
                        } else {
                            printInfo(PRINT_FILE_REQUEST_IS_NOT_HERE, new Object[]{fileNameInt});
                            sendFileRequest(closestPort, fileNameInt, requestPortInt);
                            printInfo(PRINT_FILE_REQUEST_FORWARD, null);
                        }

                    } else if (fileResponsePos != -1) {
                        String remanentPart = clientSentence.substring(fileResponsePos + DATA_FILE_RESPONSE.length());
                        int commaPos = remanentPart.indexOf(",");
                        String fileNameStr = remanentPart.substring(0, commaPos);
                        String fileOwner = remanentPart.substring(commaPos + 1);
                        printInfo(PRINT_FILE_REQUEST_REPONSE, new Object[]{fileNameStr, fileOwner});
                    } else if (quitPos != -1) {
                        String remanentPart = clientSentence.substring(quitPos + DATA_QUIT.length());
                        int commaPos = remanentPart.indexOf(",");
                        String quitIdStr = remanentPart.substring(0, commaPos);
                        remanentPart = remanentPart.substring(commaPos + 1);
                        commaPos = remanentPart.indexOf(",");
                        String quitFirstSuccessorStr = remanentPart.substring(0, commaPos);
                        String quitSecondSuccessorStr = remanentPart.substring(commaPos + 1);
                        int quitId = Integer.parseInt(quitIdStr);
                        int quitFirstSuccessor = Integer.parseInt(quitFirstSuccessorStr);
                        int quitSecondSuccessor = Integer.parseInt(quitSecondSuccessorStr);
                        if (firstSuccessorId == quitId) {
                            firstSuccessorId = secondSuccessorId;
                            secondSuccessorId = quitSecondSuccessor;
                        } else if (secondSuccessorId == quitId) {
                            secondSuccessorId = quitFirstSuccessor;
                        }
                        printInfo(PRINT_QUIT_NOTIFICATION, new Object[]{quitId});
                        printInfo(PRINT_QUIT_CHANGE, new Object[]{firstSuccessorId, secondSuccessorId});
                        sendPredecessors(secondSuccessorId, peerId, firstSuccessorId);
                        isBreakPingSleep1 = true;
                        isBreakPingSleep2 = true;
                        sendClose(BASE_PORT + quitId);
                    } else if (closePos != -1) {
                        String quitIdStr = clientSentence.substring(closePos + DATA_CLOSE.length());
                        int quitId = Integer.parseInt(quitIdStr) + BASE_PORT;
                        if (quitId == predecessorsId1) {
                            isResponseFromPredecessors1 = true;
                        }
                        if (quitId == predecessorsId2) {
                            isResponseFromPredecessors2 = true;
                        }
                        if (isResponseFromPredecessors1 && isResponseFromPredecessors2) {
                            isQuit = true;
                        }

                    } else if (successorsRequestPos != -1) {
                        String requestIdStr = clientSentence.substring(successorsRequestPos + DATA_SUCCESSORS_REQUEST.length());
                        int reuqestId = Integer.parseInt(requestIdStr) + BASE_PORT;
                        sendSuccessorsResponse(reuqestId);

                    } else if (successorsResponsePos != -1) {
                        String remanentPart = clientSentence.substring(successorsResponsePos + DATA_SUCCESSORS_RESPONSE.length());
                        int commaPos = remanentPart.indexOf(",");
                        String requestIdStr = remanentPart.substring(0, commaPos);
                        remanentPart = remanentPart.substring(commaPos + 1);
                        commaPos = remanentPart.indexOf(",");
                        String firstSuccessorStr = remanentPart.substring(0, commaPos);
                        String secondSuccessorStr = remanentPart.substring(commaPos + 1);
                        int responseId = Integer.parseInt(requestIdStr);
                        int s_firstSuccessor = Integer.parseInt(firstSuccessorStr);
                        int s_secondSuccessor = Integer.parseInt(secondSuccessorStr);
                        aliveSuccessorInfo[0] = responseId;
                        aliveSuccessorInfo[1] = s_firstSuccessor;
                        aliveSuccessorInfo[2] = s_secondSuccessor;
                        if (responseId == firstSuccessorId) {
                            pingRequest2.timeout = PingRequestThread.TIMEOUT_START_CHANGING_SUCCESSOR;
                        } else if (responseId == secondSuccessorId) {
                            pingRequest1.timeout = PingRequestThread.TIMEOUT_START_CHANGING_SUCCESSOR;
                        }
                    } else if (predecessorPos != -1) {
                        String remanentPart = clientSentence.substring(predecessorPos + DATA_PREDECESSOR.length());
                        int commaPos = remanentPart.indexOf(",");
                        String firstPredecessor = remanentPart.substring(0, commaPos);
                        String secondPredecessor = remanentPart.substring(commaPos + 1);
                        predecessorsId1 = Integer.parseInt(firstPredecessor) + BASE_PORT;
                        predecessorsId2 = Integer.parseInt(secondPredecessor) + BASE_PORT;
//                        System.out.println("update predecessors " + predecessorsId1 + "/" + predecessorsId2);
                    } else if (missingPos != -1) {
                        String remanentPart = clientSentence.substring(missingPos + DATA_MISSING.length());
                        int commaPos = remanentPart.indexOf(",");
                        String firstSuccessorStr = remanentPart.substring(0, commaPos);
                        remanentPart = remanentPart.substring(commaPos + 1);
                        commaPos = remanentPart.indexOf(",");
                        String secondSuccessorStr = remanentPart.substring(0, commaPos);
                        String missingIdStr = remanentPart.substring(commaPos + 1);
                        int first = Integer.parseInt(firstSuccessorStr);
                        int second = Integer.parseInt(secondSuccessorStr);
                        int missingId = Integer.parseInt(missingIdStr);
                        firstSuccessorId = first;
                        secondSuccessorId = second;
                        printInfo(PRINT_FORCE_QUIT, new Object[]{missingId});
                        printInfo(PRINT_QUIT_CHANGE, new Object[]{firstSuccessorId, secondSuccessorId});
                        sendPredecessors(secondSuccessorId, peerId, firstSuccessorId);
                    }

                } catch (Exception e) {
//                    System.out.println(e.toString());
                }
            }
        }
    }

    private void printInfo(int id, Object[] args) {
        switch (id) {
            case PRINT_PING_REQUEST:
                System.out.println("A ping request message was received from Peer " + ((int) args[0] - BASE_PORT) + ".");
                break;
            case PRINT_PING_RESPONSE:
                System.out.println("A ping response message was received from Peer " + ((int) args[0] - BASE_PORT) + ".");
                break;
            case PRINT_FILENAME_OUT_OF_BOUNDARY:
                System.out.println("The file name should between 1 to 9999. Your input is " + args[0] + ".");
                break;
            case PRINT_FILENAME_INVALID:
                System.out.println("The file name should be a number. Your input is not a number.");
                break;
            case PRINT_FILE_REQUEST_SEND:
                System.out.println("File request message for " + df.format(args[0]) + " has been sent to my successor.");
                break;
            case PRINT_FILE_REQUEST_FORWARD:
                System.out.println("File request message has been forwarded to my successor.");
                break;
            case PRINT_FILE_REQUEST_IS_HERE:
                System.out.println("File " + df.format(args[0]) + " is here.");
                break;
            case PRINT_FILE_REQUEST_IS_NOT_HERE:
                System.out.println("File " + df.format(args[0]) + " is not stored here.");
                break;
            case PRINT_FILE_REQUEST_REPONSE:
                System.out.println("Received a response message from peer " + args[1] + ", which has the file " + df.format(args[0]) + ".");
                break;
            case PRINT_FILE_REQINT_SEND_FILE:
                System.out.println("A response message, destined for peer " + args[0] + ", has been sent.");
                break;
            case PRINT_QUIT_NOTIFICATION:
                System.out.println("Peer " + args[0] + " will depart from the network.");
                break;
            case PRINT_QUIT_CHANGE:
                System.out.println("My first successor is now peer " + args[0] + ".");
                System.out.println("My second successor is now peer " + args[1] + ".");
                break;
            case PRINT_FORCE_QUIT:
                System.out.println("peer " + args[0] + " is no longer alive.");
                break;


        }
    }

    /**
     * parse argument(String) to int
     *
     * @param str argument_string
     * @return argument_int
     */
    private int string2Int(String str) {
        int result = -1;
        try {
            result = Integer.parseInt(str.trim());
        } catch (Exception e) {
            System.out.println("argument " + str + " can not be parsed to int");
        }

        return result;
    }


}
