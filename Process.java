/*
COMP2240 Assignment 3
File: Process.java
Author: Timothy Kemmis
Std no. c3329386
Description: A Process object that holds all of the information unique to the process; the trace of all the page requests of the process, the memory allocated to the process,
process ID, process file name, the list of page fault times of the process, turnaround time and a IO Controller for the process.
The process has various methods that check if the next page needed is in memory and if not then an IO request is made by getting the IO swap time, calculating when the page arrives
in memory and then using the IO controller to swap the page into memory using the page replacement policy set by the mode of the controller. A run method executes the instruction of the
page and then page faults.
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

    //Constructor that initialises process variables and reads through the process file to create the trace of page calls
    public Process(int id, Scanner fileReader, int sizeOfMemory, String fName){
        ID = id;
        pMemory = new int[sizeOfMemory];//Memory is an array of integers and with a size corresponding to the number of frames allocated to the process
        blocked = false;
        fileName = fName;

        IOController = new IOController(sizeOfMemory,pMemory);//Creates a new IO Controller for the process which has access to the memory of the process and deals with page replacement
        fileReader.next();//This would be "begin" in the current data file formatting
        String in = fileReader.next();//The first page request number
        while (!in.equalsIgnoreCase("end")){//while loop that loops through the process file and adds all the page requests to the ArrayList
            pageRequestList.add(Integer.parseInt(in));
            in = fileReader.next();
        }
    }

    //Returns true if the process either can continue running or has finished
    public boolean run(){
        //A method that runs the process by executing the current page. Will only be used when the process is ready (i.e. the page to be executed has been transferred into memory)
        //This function will execute one page instruction. Then either it will stop and issue a page fault if necessary.

        A3.incrementTime();//Executes the page to be executed which is currently in memory
        if (pageRequestList.size()==0){//If all of the pages have been executed then the process is finished execution
            turnaroundTime = A3.getTime();//set the turnaround time and return true to indicate finishing
            return true;
        }
        currentPage = pageRequestList.get(0);//Gets the next page to be executed and removes it from the request list
        pageRequestList.remove(0);
        if (!isPageInMemory(currentPage)){//If the page is not in memory then issue a page fault and request the page
            pageArrivalTime = A3.getTime() + IOController.getIOTime();//Calculate the time when the page will arrive in memory from IO
            pageFaultTimes.add(A3.getTime());//Record the page fault
            waitingOnPage = true;
            blocked = true;
            return false;
        } else {
            putPageInMemory();//puts or updates the next page in memory with whichever means is necessary
            waitingOnPage = false;
            return true;
        }

    }

    public void begin(){//Method to start the process
        currentPage = pageRequestList.get(0);//gets the next page to be executed
        pageRequestList.remove(0);
        pageArrivalTime = A3.getTime() + IOController.getIOTime();//Calculates when the page will arrive from IO and issues a page fault
        pageFaultTimes.add(A3.getTime());
        waitingOnPage = true;
        blocked = true;
    }

    public boolean isPageInMemory(int pageIDNum){//Checks if a given page is in memory
        for (int i: pMemory){
            if (pageIDNum==i){
                return true;
            }
        }
        return false;
    }

    public boolean isMemoryFull(){//Checks if there are are free spaces remaining in memory
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
            if (pageArrivalTime==A3.getTime()){//Transfers the page into memory if the IO wait is finished
                putPageInMemory();//Puts the page in memory. Function handles what to do in each of the possible scenarios
                waitingOnPage = false;
                return true;//The page is in memory so return true.
            } else {
                return false;//Otherwise return false as the page is not in memory
            }
        }

        return true;//If we are not waiting on a page then it is in memory
    }

    public void putPageInMemory(){
        //Called when the I/O wait is over and has transferred the page. Put the page into memory using the I/O controller.
        // The method either uses the page that is in memory, puts the page in an empty memory slot or replaces using the set replacement policy of the controller.
        //current page is the page that is currently going to be executed which is being put in memory
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

    public boolean hasFinished(){//Method that returns true when the process has finished running all it's pages
        if (pageRequestList.size()==0 && turnaroundTime!=0){//The process has finished if there are no more pages to request and the turnaround time has been written to.
            return true;
        }
        return false;
    }

    @Override
    public String toString(){//Outputs the statistics of the process as per the spec
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
    public int compareTo(Process p) {//CompareTo method used for sorting the processes
        if (this.ID>p.ID)
            return 1;
        else if (this.ID<p.ID)
            return -1;
        return 0;
    }
}
