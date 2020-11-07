import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class A3 {

    private static int time = 0;//Global time variable

    public static void main(String args[]){
        int frames = 0, quantum = 0;//Number of total frames and RR quantum time variables
        //2 ArrayLists for the same set of processes. They will be each used for doing a different page replacement policy
        ArrayList<Process> processes1 = new ArrayList<>(), processes2 = new ArrayList<>();
        try {//A try catch statement that initialises program settings for total number of memory frames and quantum time for RR.
            frames = Integer.parseInt(args[0]);
            quantum = Integer.parseInt(args[1]);
            int numProcesses = args.length-2;
            int framesPerProcess = frames/numProcesses;

            //Creates process objects for all of the process files entered as input parameters. A list of processes for each page replacement policy
            for (int i = 0; i < numProcesses; i++){
                processes1.add(new Process(i+1, new Scanner(new FileInputStream(args[i+2])), framesPerProcess, args[i+2]));
                processes2.add(new Process(i+1, new Scanner(new FileInputStream(args[i+2])), framesPerProcess, args[i+2]));
            }
        } catch (Exception e){
            System.err.println(e);
            System.exit(-1);
        }

        for (int i = 1; i < 3; i++) {//Runs RR twice, once for LRU and once for clock. i is used to set the replacement policy of the processes
            if (i==1){
                RR(i, quantum, processes1);//Run RR with LRU
            } else {
                RR(i, quantum, processes2);//Run RR with clock
            }
        }

        //Outputting the program data as per the spec
        System.out.println("LRU - Fixed:");
        System.out.println("PID\tProcess Name\tTurnaround Time\t\t# Faults\tFault Times");
        for (Process p: processes1){
            System.out.println(p);
        }
        System.out.println("\n---------------------------------------------------------------------");
        System.out.println("Clock - Fixed:");
        System.out.println("PID\tProcess Name\tTurnaround Time\t\t# Faults\tFault Times");
        for (Process p: processes2){
            System.out.println(p);
        }

    }

    //Method that does RR scheduling for a list of processes with a specified page replacement mode
    public static void RR(int replacementMode, int quantum, ArrayList<Process> allProcesses){
        ArrayList<Process> readyQueue = new ArrayList<>(), blockedQueue = new ArrayList<>();//Queues for holding the ready and blocked processes
        Process executing = null;//The process that will be the currently executing process
        int quantumStartTime = 0;//The time when the process was started to be used for calculating if the process has been running for quantum time
        resetTime();//Resets the time so the simulation can begin again if RR has already been run
        for (Process p:allProcesses){
            p.setControllerMode(replacementMode);//Sets the replacement policy to be used by the IO controller when replacing pages in memory for this process
            p.begin();//Begins all the processes which just page faults all of them
            blockedQueue.add(p);//Adds all processes to blocked queue as they will all start wil no pages in memory and so will all page fault
        }

        boolean allProcessesFinished = false;//While loop condition
        while (!allProcessesFinished){//while loop to schedule and run all of the processes with RR
            for (Process p:allProcesses){
                p.checkPage();//A method that will check if any waiting page requests are due to enter memory. If they are then the IO controller is used to bring the page into memory
            }
            updateReadyQueue(blockedQueue,readyQueue);//Updates the readyQueue with all the blocked processes that are now ready

            if (readyQueue.size()>0 || executing!=null) {//A check that deals with running processes from the readyQueue or continuing to run an executing process
                if (executing==null) {
                    executing = readyQueue.get(0);//get the next process with the highest priority from the readyQueue
                    readyQueue.remove(executing);
                    quantumStartTime = A3.getTime();
                    if (!executing.run()){
                        blockedQueue.add(executing);
                        executing = null;
                    } else if (executing.hasFinished()){
                        executing = null;
                    }
                } else if (A3.getTime()-quantumStartTime==quantum){//if quantum time has passed for the running process then swap running process
                    if (executing.checkPage()){
                        readyQueue.add(executing);
                    } else {
                        blockedQueue.add(executing);
                    }

                    executing = readyQueue.get(0);//get the next process with the highest priority from the readyQueue
                    readyQueue.remove(executing);
                    quantumStartTime = A3.getTime();
                    if (!executing.run()){
                        blockedQueue.add(executing);
                        executing = null;
                    } else if (executing.hasFinished()){
                        executing = null;
                    }
                } else {//If the process can still run then run it again
                    if (!executing.run()){
                        blockedQueue.add(executing);
                        executing = null;
                    } else if (executing.hasFinished()){
                        executing = null;
                    }
                }

                if (blockedQueue.isEmpty() && readyQueue.isEmpty() && executing==null) {
                    allProcessesFinished = true;
                }
            }

            if (readyQueue.isEmpty()&&executing==null&&!pagesArrive(blockedQueue))
                incrementTime();

        }


    }

    public static void updateReadyQueue(ArrayList<Process> blockedQueue, ArrayList<Process> readyQueue){//checks if any processes are ready and if so adds them to the readyQueue
        //Check each process in the blocked queue and if they are ready then add them to the readyQueue and remove from the blocked queue
        ArrayList<Process> addingToReadyQueue= new ArrayList<>();
        for (Process p:blockedQueue){
            //Unfinished
            if (p.checkPage()){
                addingToReadyQueue.add(p);
            }
        }
        for (Process p: addingToReadyQueue){
            blockedQueue.remove(p);
        }
        addingToReadyQueue.sort(Process::compareTo);
        readyQueue.addAll(addingToReadyQueue);
    }

    public static boolean pagesArrive(ArrayList<Process> blockedQueue){//check to see if any pages will be be transferred into memory at the current time
        for (Process p:blockedQueue){
            if (p.checkPage()){
                return true;
            }
        }
        return false;
    }

    public static void resetTime(){//Resets the time to 0
        time = 0;
    }

    public static void incrementTime(){
        time++;
    }

    public static int getTime(){
        return time;
    }
}
