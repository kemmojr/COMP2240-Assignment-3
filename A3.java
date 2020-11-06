import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

public class A3 {

    public static int time = 0;
    public static void main(String args[]){
        int frames = 0, quantum = 0;//Number of total frames and RR quantum time variables
        ArrayList<Process> processes1 = new ArrayList<>(), processes2 = new ArrayList<>();
        try {
            frames = Integer.parseInt(args[0]);
            quantum = Integer.parseInt(args[1]);
            int numProcesses = args.length-2;
            int framesPerProcess = frames/numProcesses;

            for (int i = 0; i < numProcesses; i++){
                processes1.add(new Process(i+1, new Scanner(new FileInputStream(args[i+2])), framesPerProcess));
                processes2.add(new Process(i+1, new Scanner(new FileInputStream(args[i+2])), framesPerProcess));
            }
        } catch (Exception e){
            System.err.println(e);
        }

        for (int i = 1; i < 3; i++) {
            if (i==1){
                RR(i, quantum, processes1);//Run RR with LRU
            } else {
                RR(i, quantum, processes2);//Run RR with clock
            }
        }

        System.out.println("Print out all the program statistics for each process for LRU and clock page replacement policies");


        /*boolean allProcessesFinished = false;
        while (!allProcessesFinished){//while loop to contain the RR scheduling of the processes
            allProcessesFinished = true;
            //incrementTime()
        }*/
    }

    public static void RR(int replacementMode, int quantum, ArrayList<Process> allProcesses){
        //Do round robin scheduling with all of the processes
        ArrayList<Process> readyQueue = new ArrayList<>(), blockedQueue = new ArrayList<>();
        Process executing = null;//The process that will be the currently executing process
        int quantumTimeRemaining = quantum, quantumStartTime = 0;
        resetTime();
        for (Process p:allProcesses){
            p.setControllerMode(replacementMode);
            p.begin();//Begins all the processes which just page faults all of them
            blockedQueue.add(p);
        }

        boolean allProcessesFinished = false;
        while (!allProcessesFinished){//while loop to
            // contain the RR scheduling of the processes and the necessary methods for updating memory in processes etc.
            for (Process p:allProcesses){
                p.checkPage();
            }
            updateReadyQueue(blockedQueue,readyQueue);
            //allProcessesFinished = true;//This will be moved to when the condition of all the processes have finished is actually true
            //RR scheduling here

            if (readyQueue.size()>0) {

                    if (executing==null) {
                        executing = readyQueue.get(0);//get the next process with the highest priority from the readyQueue
                        readyQueue.remove(executing);
                        quantumStartTime = A3.getTime();
                        if (!executing.run()){
                            blockedQueue.add(executing);
                            executing = null;
                        }
                    } else if (A3.getTime()-quantumStartTime==quantum){//if quantum time has passed for the running process then swap running process
                        blockedQueue.add(executing);
                        executing = readyQueue.get(0);//get the next process with the highest priority from the readyQueue
                        readyQueue.remove(executing);
                        quantumStartTime = A3.getTime();
                        if (!executing.run()){
                            blockedQueue.add(executing);
                            executing = null;
                        }
                    } else {//If the process can still run then run it again
                        if (!executing.run()){
                            blockedQueue.add(executing);
                            executing = null;
                        }
                    }


                if (blockedQueue.isEmpty() && readyQueue.isEmpty() && executing==null) {
                    allProcessesFinished = true;
                }
            }

            if (quantumTimeRemaining ==0){//Pre-emption with quantum time
                if (readyQueue.size()<0 && !executing.checkPage()){
                    //continue running the process without running the dispatcher
                    quantumTimeRemaining = quantum;
                    quantumStartTime = A3.getTime();
                    if (executing.run()){//If the process becomes blocked after running
                        blockedQueue.add(executing);
                        executing = null;
                    }
                } else if (readyQueue.size()>0){//If there are processes in the readyQueue then swap to the process at teh top of the readyQueue
                    if (executing.checkPage()){
                        readyQueue.add(executing);//If the process is not blocked then add it back to the end of the ready queue
                    }
                    executing = readyQueue.get(0);
                    readyQueue.remove(executing);
                    if (!executing.run()){//If the process becomes blocked when it is run then add it to the blocked queue
                        blockedQueue.add(executing);
                    }
                    quantumTimeRemaining = quantum;
                    quantumStartTime = A3.getTime();
                }


            }
            if (readyQueue.isEmpty()&&executing==null)
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
