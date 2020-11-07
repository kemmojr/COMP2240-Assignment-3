/*
COMP2240 Assignment 3
File: A3.java
Author: Timothy Kemmis
Std no. c3329386
Description: A program to simulate RR scheduling of a set of inputted processes and the memory management of the paging and virtual memory of these processes.
The program has a limited number of frames (inputted as a parameter) which must be shared equally between all of the processes. As the memory allocated to the process does not change
over time, I handled all of the memory management of the processes in the process object. Each process has a block of memory corresponding to the frames allocated to the process.
The each process has a corresponding page controller that handles replacing the pages for that process.
An inputted quantum of Q is used in the RR scheduling. The program handles two forms of page replacement policies; LRU and Clock.
A3 has the program initialisation and the RR scheduling of the processes
*/

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

        //Outputting the data of all of the processes as per the spec
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
            blockedQueue.add(p);//Adds all processes to blocked queue as they will all start with no pages in memory and so will all page fault and become blocked
        }

        boolean allProcessesFinished = false;//While loop condition
        while (!allProcessesFinished){//while loop to schedule and run all of the processes with RR
            for (Process p:allProcesses){//loops through all the processes being scheduled
                p.checkPage();//A method that will check if any waiting page requests are due to enter memory. If they are then the IO controller is used to bring the page into memory
            }
            updateReadyQueue(blockedQueue,readyQueue);//Updates the readyQueue with all the blocked processes that are now ready

            if (readyQueue.size()>0 || executing!=null) {//Check to run processes from the readyQueue or continuing to run the executing process
                if (executing==null) {//If there is no executing process then run the next process from the readyQueue
                    executing = readyQueue.get(0);//get the process at the front of the readyQueue and then remove it from the queue
                    readyQueue.remove(executing);
                    quantumStartTime = A3.getTime();//Sets the time that the process was started from for calculating if the quantum time for the running process has been reached
                    if (!executing.run()){//Runs the process, executing its page instruction. run() returns false if the process becomes blocked & true if the process is still ready
                        blockedQueue.add(executing);//If the process becomes blocked then add it to the blockedQueue
                        executing = null;
                    } else if (executing.hasFinished()){
                        executing = null;//If the process has finished executing then remove it from the processes being simulated by setting it to null.
                        // As we are only using blocked and readyQueue for scheduling it will be discarded
                    }
                } else if (A3.getTime()-quantumStartTime==quantum){//if quantum time has passed for the running process then preempt and swap the running process
                    if (executing.checkPage()){//Put the process in the queue that matches its state
                        readyQueue.add(executing);
                    } else {
                        blockedQueue.add(executing);
                    }

                    executing = readyQueue.get(0);//get the process at the front of the readyQueue and then remove it from the queue
                    readyQueue.remove(executing);
                    quantumStartTime = A3.getTime();//Sets the time that the process was started from for calculating if the quantum time for the running process has been reached
                    if (!executing.run()){//Runs the process, executing its page instruction. run() returns false if the process becomes blocked & true if the process is still ready
                        blockedQueue.add(executing);//If the process becomes blocked then add it to the blockedQueue
                        executing = null;
                    } else if (executing.hasFinished()){
                        executing = null;
                    }
                } else {//If the process is still ready and quantum time has not been reached then run it again
                    if (!executing.run()){//Runs the process, executing its page instruction. run() returns false if the process becomes blocked & true if the process is still ready
                        blockedQueue.add(executing);//If the process becomes blocked then add it to the blockedQueue
                        executing = null;
                    } else if (executing.hasFinished()){
                        executing = null;//If the process has finished executing then remove it from the processes being simulated by setting it to null.
                        // As we are only using blocked and readyQueue for scheduling it will be discarded
                    }
                }

                if (blockedQueue.isEmpty() && readyQueue.isEmpty() && executing==null) {
                    allProcessesFinished = true;//Exit RR when all processes have been scheduled and run
                }
            }

            if (readyQueue.isEmpty()&&executing==null&&!pagesArrive(blockedQueue))//Increment the time if there is nothing that can be done at the current time
                incrementTime();

        }


    }

    public static void updateReadyQueue(ArrayList<Process> blockedQueue, ArrayList<Process> readyQueue){//checks if any processes are ready and if so adds them to the readyQueue
        //Check each process in the blocked queue and if they are ready then add them to an ArrayList of processes to be added.
        ArrayList<Process> addingToReadyQueue= new ArrayList<>();
        for (Process p:blockedQueue){
            if (p.checkPage()){
                addingToReadyQueue.add(p);
            }
        }
        //Remove the ready processes from the blocked queue
        for (Process p: addingToReadyQueue){
            blockedQueue.remove(p);
        }
        //Adding processes are sorted as when multiple processes are being added to the readyQueue at the one time then they will enter in order of ID
        addingToReadyQueue.sort(Process::compareTo);
        readyQueue.addAll(addingToReadyQueue);//Add all of the newly ready processes to the readyQueue
    }

    //Method used for checking if we can increment the time. Ensures that process switching occurs at the same time as another process page faults
    public static boolean pagesArrive(ArrayList<Process> blockedQueue){//check to see if any pages will be be transferred into memory at the current time as the IO page swapping wait is over
        for (Process p:blockedQueue){
            if (p.checkPage()){//For each process in the blockedQueue, are there any that become ready at the current time
                return true;
            }
        }
        return false;
    }

    public static void resetTime(){//Resets the time to 0
        time = 0;
    }

    public static void incrementTime(){//increments the time
        time++;
    }

    public static int getTime(){//gets the current time
        return time;
    }
}
