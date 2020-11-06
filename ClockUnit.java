public class ClockUnit {
    private int page;//the page number that this clock unit represents
    private boolean useBit;//The bit used in the clock method of memory management
    private ClockUnit nextU;//next and previous pointers

    public ClockUnit(int pageNum, boolean bit){//Constructor for first unit
        page = pageNum;
        useBit = bit;
        nextU = null;
    }

    public void setNext(ClockUnit nxt){
        nextU = nxt;
    }

    public void setPage(int pageNum){//updates the page stored in the clock unit and updates the use bit to match
        page = pageNum;
        useBit = true;
    }

    public void setUseBit(boolean useBitSetting){
        useBit = useBitSetting;
    }

    public ClockUnit getNext(){
        return nextU;
    }

    public boolean getUseBit(){
        return useBit;
    }

    public int getPage(){
        return page;
    }

    public ClockUnit passOver(){//method for passing over a clock unit and resetting the use bit to 0. Returns the next unit
        useBit = false;
        return nextU;
    }

}
