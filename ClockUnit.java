/*
COMP2240 Assignment 3
File: ClockUnit.java
Author: Timothy Kemmis
Std no. c3329386
 */

public class ClockUnit {//A node to be used to create a circular linked list for the clock page replacement policy
    private int page;//the page number that this clock unit represents
    private boolean useBit;//The bit used in the clock method of memory management
    private ClockUnit nextU;//next and previous pointers

    public ClockUnit(int pageNum, boolean bit){//Constructor which initialises the ClockUnit with a page, useBit and next pointer
        page = pageNum;
        useBit = bit;
        nextU = null;
    }

    public void setNext(ClockUnit nxt){//Sets the next pointer of the ClockUnit node
        nextU = nxt;
    }

    public void setPage(int pageNum){//updates the page stored in the clock unit and updates the use bit to match
        page = pageNum;
        useBit = true;
    }

    public void setUseBit(boolean useBitSetting){//Sets the UseBit
        useBit = useBitSetting;
    }

    public ClockUnit getNext(){//Gets the next pointer of the ClockUnit
        return nextU;
    }

    public boolean getUseBit(){//gets the value of the UseBit
        return useBit;
    }

    public int getPage(){//Gets the value of the page
        return page;
    }

    public ClockUnit passOver(){//method for passing over a clock unit and resetting the use bit to 0. Returns the next unit
        useBit = false;
        return nextU;
    }

}
