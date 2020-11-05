import java.util.ArrayList;

public class IOController {
    private int mode, memorySize;//An integer that denotes which page replacement strategy the I/O controller will use. Mode 1 is LRU and 2 is Clock.
    // Memory size is for knowing how much memory is available for the process this IOController is assigned to
    private int IOSwapTime;//A variable for the amount of time that it takes for a page to be transferred from IO to main memory
    private ArrayList<Integer> recentlyUsedPages = new ArrayList<>();//An ArrayList that stores the order in which the current frames in memory have been used from most recent to least
    private ArrayList<ClockUnit> clock = new ArrayList<>();//An arrayList to be used for the clock page replacement strategy
    private ClockUnit nextClockFrame;
    private int[] memory;

    public IOController(int numFrames, int[] processMemory){
        int mode = 0;//Sets the mode to a non-functioning mode 0. The controller will need to be told which page replacement strategy to use with setMode()
        memorySize = numFrames;
        IOSwapTime = 6;
        memory = processMemory;

        //initialise an empty clock for clock replacement policy
        for (int i = 0; i < numFrames; i++) {
            clock.add(new ClockUnit(0,false));//Creates the empty circular buffer. Use bit is false as technically the frame has not been used
            if (i>0){
                clock.get(i-1).setNext(clock.get(i));
            }
        }
        clock.get(numFrames-1).setNext(clock.get(0));//Sets the next of the last clock unit to the first, making it circular
        nextClockFrame = clock.get(0);
    }

    public void pageInFreeSlot(int pageIn){//puts a page in the first available free memory slot. Is only called when memory has a free space
        int insertionFrameNum = 0;
        for (int i = 0; i < memorySize; i++){
            if (memory[i]==0){//if the frame is empty then put the page in
                memory[i] = pageIn;
                insertionFrameNum = i;
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

    public int getIOTime(){
        return IOSwapTime;
    }

    public void swapPageIn(int pageNum){
        if (mode==1){
            //bring the page into memory and use LRU page replacement policy when page replacement is necessary
            Integer leastUsedFrame = recentlyUsedPages.get(recentlyUsedPages.size()-1);
            recentlyUsedPages.remove(leastUsedFrame);
            memory[getIndexOf(leastUsedFrame)] = pageNum;
            recentlyUsedPages.add(0, pageNum);

        } else if (mode ==2){
            //bring the page into memory and use clock page replacement policy when page replacement is necessary
            int replacedPage = 0;
            boolean pageInserted = false;
            while (!pageInserted){
                if (!nextClockFrame.getUseBit()){
                    replacedPage = nextClockFrame.getPage();
                    nextClockFrame.setPage(pageNum);
                    nextClockFrame = nextClockFrame.getNext();
                    pageInserted = true;
                } else {
                    nextClockFrame = nextClockFrame.passOver();
                }
            }
            memory[getIndexOf(replacedPage)] = pageNum;
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
