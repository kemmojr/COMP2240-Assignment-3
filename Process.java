/*
COMP2240 Assignment 3
File: Process.java
Author: Timothy Kemmis
Std no. c3329386
Description: A Process object that holds all of the information unique to the process; the trace of all the page requests of the process, the memory allocated to the process,
process ID, process file name, the list of page fault times of the process, turnaround time and a IO Controller for the process.

*/

import java.util.ArrayList;
import java.util.Scanner;

//Implements comparable for sorting when adding multiple processes to the ready queue at the one time
public class Process implements Comparable<Process>{
    private int ID, currentPage = 0, pageArrivalTime = 0, turnaroundTime = 0;//The ID, page to be executed, time that the requested page arrives from IO and process turnaround time
    private ArrayList<Integer> pageRequestList = new ArrayList<>(), pageFaultTimes = new ArrayList<>();// ArrayLists for the trace of page requests and page fault times of the process
    private int[] pMemory;//The block of memory assigned to the process
    private boolean blocked, waitingOnPage;//Variables for knowing what state the process is in
    private IOController IOController;//The IO Controller that deals with the paging of the process memory
    private String fileName;//The filename that corresponds to the process

    public Process(int id, Scanner fileReader, int sizeOfMemory, String fName){
        ID = id;
        pMemory = new int[sizeOfMemory];
        blocked = false;

        IOController = new IOController(sizeOfMemory,pMemory);
        fileReader.next();//This would be begin in the current data file formatting
        String in = fileReader.next();
        while (!in.equalsIgnoreCase("end")){
            pageRequestList.add(Integer.parseInt(in));
            in = fileReader.next();
        }
        fileName = fName;

    }

    //Returns true if the process either can continue running or has finished
    public boolean run(){//A function that runs the process by executing the current page. Will only be used when the process is ready (i.e. the frame requested has been transferred)
        //This function will execute one page instruction. Then either it will stop and issue a page fault if necessary.
        //The main loop will handle stopping for quantum time etc
        /*executes the page and increments the time
        Moves on to the next page in the page request list
        executes it if the page is in memory
        otherwise it issues a page fault and becomes blocked*/
        //Page should be in memory at this point. Execute the page instruction and then get the next instruction
        A3.incrementTime();//Executes the page to be executed which is currently in memory
        if (pageRequestList.size()==0){//If all of the pages have been executed then the process is finished execution
            turnaroundTime = A3.getTime();
            return true;
        }
        currentPage = pageRequestList.get(0);
        pageRequestList.remove(0);
        if (!isPageInMemory(currentPage)){
            pageArrivalTime = A3.getTime() + IOController.getIOTime();
            pageFaultTimes.add(A3.getTime());
            waitingOnPage = true;
            blocked = true;
            return false;
        } else {
            putPageInMemory();//puts or updates the next page in memory with whichever means is necessary
            waitingOnPage = false;
            return true;
        }

    }

    public void begin(){//Method for the first time the process is run
        currentPage = pageRequestList.get(0);
        pageRequestList.remove(0);
        pageArrivalTime = A3.getTime() + IOController.getIOTime();
        pageFaultTimes.add(A3.getTime());
        waitingOnPage = true;
        blocked = true;
    }

    public boolean isPageInMemory(int pageIDNum){
        for (int i: pMemory){
            if (pageIDNum==i){
                return true;
            }
        }
        return false;
    }

    public boolean isMemoryFull(){
        for (int i:pMemory){
            if (i==0){
                return false;
            }
        }
        return true;
    }

    //Returns true if the page is in memory
    public boolean checkPage(){//Checks if the next page has reached the time when it enters memory and puts it in if the time has been reached
        if (waitingOnPage){
            if (pageArrivalTime==A3.getTime()){
                putPageInMemory();//Puts the page in memory. Function handles what to do in each of the possible scenarios
                waitingOnPage = false;
                return true;
            } else {
                return false;
            }
        }

        return true;//If we are not waiting on a page then it is in memory
    }

    public void putPageInMemory(){
        //Called when the I/O wait is over and has transferred the page. Put the page into memory using the I/O controller. Also update the ready state of the process

        if (isPageInMemory(currentPage)){//Update the LRU pages in the I/O controller
            IOController.usePageInMemory(currentPage);
        } else if (!isMemoryFull()){//Is there a free slot in memory. If so then put the page in the free slot
            IOController.pageInFreeSlot(currentPage);
        } else {
            IOController.swapPageIn(currentPage);
        }
    }

    public void setControllerMode(int modeNumber){//sets the page replacement mode of the IOController
        IOController.setMode(modeNumber);//mode 1 LRU. mode 2 clock
    }

    public boolean hasFinished(){
        if (pageRequestList.size()==0 && turnaroundTime!=0){//The process has finished if there are no more pages to request and the turnaround time has been written to.
            return true;
        }
        return false;
    }

    @Override
    public String toString(){
        String out = ID + "\t" + fileName + "\t" + turnaroundTime + "\t\t\t\t\t" + pageFaultTimes.size() + "\t\t\t{";
        for (int i = 0; i < pageFaultTimes.size(); i++){
            if (i==pageFaultTimes.size()-1)
                out += pageFaultTimes.get(i) + "}";
            else
                out += pageFaultTimes.get(i) + ", ";
        }
        return out;
    }

    @Override
    public int compareTo(Process p) {
        if (this.ID>p.ID)
            return 1;
        else if (this.ID<p.ID)
            return -1;
        return 0;
    }
}
