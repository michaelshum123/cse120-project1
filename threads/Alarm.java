package nachos.threads;

import nachos.machine.*;
import java.util.ArrayList;
import java.util.Iterator;
/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
  /**
   * Threads on hold stored in this list of KThreads
   */
 
  private class KThreadTime{
    public KThread kt;
    public long time;
    public KThreadTime(KThread kthread, long waitUntil) {
      kt = kthread;
      time = waitUntil;
    }
  }

   private ArrayList<KThreadTime> waitingList;
  
	/**
	 * Allocate a new Alarm. Set the machine's timer interrupt handler to this
	 * alarm's callback.
	 * 
	 * <p>
	 * <b>Note</b>: Nachos will not function correctly with more than one alarm.
	 */
	public Alarm() {
    waitingList = new ArrayList<KThreadTime>();

		Machine.timer().setInterruptHandler(new Runnable() {
			public void run() {
				timerInterrupt();
			}
		});
	}

	/**
	 * The timer interrupt handler. This is called by the machine's timer
	 * periodically (approximately every 500 clock ticks). Causes the current
	 * thread to yield, forcing a context switch if there is another thread that
	 * should be run.
	 */
	public void timerInterrupt() {
    //check if any threads on list are due to be put on ready
    if( waitingList.size() > 0 ) {
      Iterator<KThreadTime> it = waitingList.iterator();
      while(it.hasNext()){
        long wakeTime = Machine.timer().getTime(); 
        KThreadTime ktt = it.next();
        if( ktt.time >= wakeTime ) { //if done waiting 
          ktt.kt.ready(); //im ready!!
          it.remove(); //get me out of here 
          it = waitingList.iterator(); // reset due to unknown behavior
        }
      }
    }  
    //KThread.currentThread().yield();
	}

	/**
	 * Put the current thread to sleep for at least <i>x</i> ticks, waking it up
	 * in the timer interrupt handler. The thread must be woken up (placed in
	 * the scheduler ready set) during the first timer interrupt where
	 * 
	 * <p>
	 * <blockquote> (current time) >= (WaitUntil called time)+(x) </blockquote>
	 * 
	 * @param x the minimum number of clock ticks to wait.
	 * 
	 * @see nachos.machine.Timer#getTime()
	 */
	public void waitUntil(long x) {
		// for now, cheat just to get something working (busy waiting is bad)
		if(x <= 0) return;
    
    long wakeTime = Machine.timer().getTime() + x;
		//insert sleeping thread onto queue?
    KThreadTime ktt = new KThreadTime(KThread.currentThread(), wakeTime);
    waitingList.add(ktt); 
    KThread.currentThread().yield();
	}
}
