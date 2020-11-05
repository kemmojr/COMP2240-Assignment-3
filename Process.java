import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class Process implements Comparable<Process>{
    private int ID, currentPage = 0, pageArrivalTime = 0, turnaroundTime = 0;//The ID, page to be executed and turnaround time of the process
    private ArrayList<Integer> pageRequestList = new ArrayList<>(), pageFaultTimes = new ArrayList<>();
    private int[] pMemory;
    private boolean blocked, pageExecuted, waitingOnPage;
    private IOController IOController;

    public Process(int id, Scanner fileReader, int sizeOfMemory){
        ID = id;
        pMemory = new int[sizeOfMemory];
        blocked = false;

        IOController = new IOController(sizeOfMemory,pMemory);
        fileReader.next();
        String in = fileReader.next();
        while (!in.equalsIgnoreCase("end")){
            pageRequestList.add(Integer.parseInt(in));
            in = fileReader.next();
        }

    }

    //Returns true if the process either can continue running or has finished
    public boolean run(){//A function that runs the process which will be used when the process becomes unblocked (i.e. the frame requested has been transferred)
        //This function will execute one page instruction. Then either it will stop and issue a page fault if necessary.
        //The main loop will handle stopping for quantum time etc
        /*executes the page and increments the time
        Moves on to the next page in the page request list
        executes it if the page is in memory
        otherwise it issues a page fault and becomes blocked*/
        //Page should be in memory at this point. Execute the page instruction and then get the next instruction
        A3.incrementTime();//Executes the page
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
            putPageInMemory();//Updates the page replacement tracking
            waitingOnPage = false;
            return true;
        }

    }

    public void begin(){//Method for the first time the process is run
        currentPage = pageRequestList.get(0);
        pageRequestList.remove(0);
        pageArrivalTime = A3.getTime() + IOController.getIOTime();
        pageFaultTimes.add(A3.getTime());
        pageExecuted = false;
        waitingOnPage = true;
        blocked = true;
    }

    public boolean isReady(){//Function that checks if the I/O request has been completed and updates the blocked variable and outputs true if it is ready
        if (!blocked){
            return true;
        }
        if (pageArrivalTime<=A3.getTime()){//If the page that was being waited on to be transferred to memory has been transferred
            blocked = false;
            return true;
        }
        return false;
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

        currentPage = 27;
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
    public int compareTo(Process p) {
        if (this.ID>p.ID)
            return 1;
        else if (this.ID<p.ID)
            return -1;
        return 0;
    }
}
