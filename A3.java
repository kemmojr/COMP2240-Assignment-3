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
                //RR(i, quantum, processes1);//Run RR with LRU
            } else {
                //RR(i, quantum, processes2);//Run RR with clock
            }

        }


        /*boolean allProcessesFinished = false;
        while (!allProcessesFinished){//while loop to contain the RR scheduling of the processes
            allProcessesFinished = true;
            //incrementTime()
        }*/
    }

    public static void RR(int replacementMode, int quantum, ArrayList<Process> allProcesses){
        //Do round robin scheduling with all of the processes
        ArrayList<Process> readyQueue = new ArrayList<>(), blockedQueue = new ArrayList<>();

        resetTime();
        for (Process p:allProcesses){
            p.setControllerMode(replacementMode);
            p.begin();//Begins all the processes which just page faults all of them
            blockedQueue.add(p);
        }

        boolean allProcessesFinished = false;
        while (!allProcessesFinished){//while loop to
            // contain the RR scheduling of the processes and the nessacary methods for updating memory in processes etc.
            for (Process p:allProcesses){
                p.checkPage();
            }
            updateReadyQueue(blockedQueue,readyQueue);
            allProcessesFinished = true;//This will be moved to when the condition of all the processes have finished is actually true
            incrementTime();
            //RR scheduling here

            /*if (processingTimeRemaining == 0) {
                if (processing != null) {//If the last process is finishing it's processing time i.e. something was just processing and the scheduler isn't starving
                    processing.setTurnAroundTime(-(processing.getArrive() - time));
                    processing.setWaitingTime(processing.getTurnAroundTime()-processing.getInitialExecSize());
                    PRRProcessed.add(processing);//metric tracking

                }
                if (readyQueue.size() > 0) {
                    time += dispatchTime;//factor in the time required to run the dispatcher
                    processing = readyQueue.get(0);//get the next process with the highest priority from the readyQueue
                    readyQueue.remove(processing);
                    processing = new SchedulerProcess(processing);
                    processingTimeRemaining = processing.getExecSize();//set how long this process has to go
                    processing.setStartTime(time);
                    PRRTimes.add(processing);
                    if (processing.isHPC()){
                        quantumTimeRemaining = quantumTimeHPC;
                    } else {
                        quantumTimeRemaining = quantumTimeLPC;
                    }
                } else if (temp.isEmpty()) {
                    allItemsExecuted = true;
                } else {
                    processing = null;
                }
            }

            if (quantumTimeRemaining ==0){//Pre-emption with quantum time
                if (readyQueue.size()<0 && processingTimeRemaining >0){
                    //continue running the process without running the dispatcher
                    if (processing.isHPC()){
                        quantumTimeRemaining = quantumTimeHPC;
                    } else {
                        quantumTimeRemaining = quantumTimeLPC;
                    }
                } else if (readyQueue.size()>0){
                    time += dispatchTime;//factor in the time required to run the dispatcher
                    if (processing!=null){
                        processing.setExecSize(processingTimeRemaining);//decrease execution time by the amount executed
                    }
                    processing = new SchedulerProcess(processing);
                    addProcessBack(processing,readyQueue);
                    processing = readyQueue.get(0);
                    readyQueue.remove(processing);
                    processing = new SchedulerProcess(processing);
                    processing.setStartTime(time);
                    PRRTimes.add(processing);
                    processingTimeRemaining = processing.getExecSize();
                    if (processing.isHPC()){
                        quantumTimeRemaining = quantumTimeHPC;
                    } else {
                        quantumTimeRemaining = quantumTimeLPC;
                    }
                }


            }
            time++;
            quantumTimeRemaining--;
            if (processing != null)
                processingTimeRemaining--;*/

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
