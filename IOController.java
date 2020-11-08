/*
COMP2240 Assignment 3
File: IOController.java
Author: Timothy Kemmis
Std no. c3329386
Description: The IO Controller that handles the swapping of pages into memory with LRU and Clock page replacement policies. Stores all the information necessary for both policies.
 */

import java.util.ArrayList;

public class IOController {
    private int mode, memorySize;//An integer that denotes which page replacement strategy the I/O controller will use. Mode 1 is LRU and 2 is Clock.
    // Memory size is for knowing how much memory is available for the process this IOController is assigned to
    private int IOSwapTime;//A variable for the amount of time that it takes for a page to be transferred from IO to main memory
    private ArrayList<Integer> recentlyUsedPages = new ArrayList<>();//An ArrayList that stores the order in which the current frames in memory have been used from most recent to least
    private ArrayList<ClockUnit> clock = new ArrayList<>();//An arrayList to be used for the clock page replacement strategy
    private ClockUnit nextClockFrame;//Pointer used in clock page replacement that points to the next clock frame in the circular clock buffer
    private int[] memory;//The memory of the process this IO controller is responsible for

    public IOController(int numFrames, int[] processMemory){
        int mode = 0;//Sets the mode to a non-functioning mode 0. The controller will need to be told which page replacement strategy to use with setMode()
        memorySize = numFrames;
        IOSwapTime = 6;
        memory = processMemory;

        //initialise an empty clock for clock replacement policy
        for (int i = 0; i < numFrames; i++) {
            clock.add(new ClockUnit(0,false));//Creates the empty circular buffer. Use bit is false as technically the page has not been used
            if (i>0){
                clock.get(i-1).setNext(clock.get(i));//Links all the clock units together in a linked list
            }
        }
        clock.get(numFrames-1).setNext(clock.get(0));//Sets the next of the last clock unit to the first, making it circular
        nextClockFrame = clock.get(0);//Sets the next clock frame pointer to the first clock unit
    }

    public void pageInFreeSlot(int pageIn){//puts a page in the first available free memory slot. Is only called when memory has a free space
        for (int i = 0; i < memorySize; i++){
            if (memory[i]==0){//if the frame is empty then put the page in
                memory[i] = pageIn;
                break;
            }
        }
        if (mode==1){
            recentlyUsedPages.add(0, pageIn);//Adds the most recently used page number at the start of the list of recently used pages
        } else if (mode==2){
            boolean pageInserted = false;
            while (!pageInserted){
                if (!nextClockFrame.getUseBit()){
                    nextClockFrame.setPage(pageIn);
                    nextClockFrame = nextClockFrame.getNext();
                    pageInserted = true;
                } else {
                    nextClockFrame = nextClockFrame.passOver();
                }
            }
        }
    }

    public void usePageInMemory(int pageNum){//updates the LRU and clock mechanisms for when a page in memory is reused
        if (mode==1){
            recentlyUsedPages.remove(new Integer(pageNum));
            recentlyUsedPages.add(0, pageNum);//Removes and re-enters the page used at the top of the list of recently used pages
        } else if (mode==2){
            //Nothing is done in clock page replacement when a frame is used
        }
    }

    public void setMode(int modeNum){//sets the page replacement strategy to be used
        mode = modeNum;
    }

    public int getIOTime(){//Gets the time taken to transfer a page from IO
        return IOSwapTime;
    }

    public void swapPageIn(int pageNum){
        if (mode==1){
            //bring the page into memory and use LRU page replacement policy when page replacement is necessary
            Integer leastUsedPage = recentlyUsedPages.get(recentlyUsedPages.size()-1);//Find the page in memory that has been used the least
            recentlyUsedPages.remove(leastUsedPage);//Remove the least used frame to ensure the list is always the same size
            memory[getIndexOf(leastUsedPage)] = pageNum;//replace the page in memory
            recentlyUsedPages.add(0, pageNum);//Update the list of recently used pages

        } else if (mode ==2){
            //bring the page into memory and use clock page replacement policy when page replacement is necessary
            int replacedPage = 0;
            boolean pageInserted = false;
            while (!pageInserted){//Cycle through the circular buffer of the clock unit until a usable page location is found
                if (!nextClockFrame.getUseBit()){
                    replacedPage = nextClockFrame.getPage();//get the page we are replacing to find in the memory later
                    nextClockFrame.setPage(pageNum);//Replace the page in the circular clock buffer and increment the clock frame pointer
                    nextClockFrame = nextClockFrame.getNext();
                    pageInserted = true;
                } else {
                    nextClockFrame = nextClockFrame.passOver();//If the current unit cannot be used then pass over it
                }
            }
            memory[getIndexOf(replacedPage)] = pageNum;//Replace the page in memory
        }
    }

    public int getIndexOf(int pageToFind){//Gets the index that a particular page is stored at
        for (int i = 0; i < memory.length; i++){
            if (memory[i]==pageToFind){
                return i;
            }
        }
        return -1;//Return -1 if the page could not be found in memory
    }


}
