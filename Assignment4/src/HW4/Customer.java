package HW4;

/**
 * @author Bo Han
 */

import java.util.Date;
import java.util.List;

/**
 * Customers are simulation actors that have two fields: a name, and a list
 * of Food items that constitute the Customer's order.  When running, an
 * customer attempts to enter the coffee shop (only successful if the
 * coffee shop has a free table), place its order, and then leave the 
 * coffee shop when the order is complete.
 */
public class Customer implements Runnable {
	//JUST ONE SET OF IDEAS ON HOW TO SET THINGS UP...
	private final String name;
	private final List<Food> order;
	private final int orderNum;

	//Assign some priority to the customer
	private int priority;
	private Date placeOrderTime;
	private Date finishOrderTime;
	private static int runningCounter = 0;

	private long eatingTime;

	/**
	 * You can feel free modify this constructor.  It must take at
	 * least the name and order but may take other parameters if you
	 * would find adding them useful.
	 */
	public Customer(String name, List<Food> order, int priority, long eatingTime) {
		this.name = name;
		this.order = order;
		this.orderNum = runningCounter++;

		this.priority = priority;
		this.eatingTime = eatingTime;
		CoffeeShop.customerByOrder.put(orderNum,this);
	}



	public String toString() {
		return name;
	}

	public int getPriority(){
		return priority;
	}

	public String getName(){

		return name;
	}

	public void setPlaceTime(){
		placeOrderTime = new Date();
	}

	public void setFinishTime(){
		finishOrderTime = new Date();
	}

	public long getWaitingTimeByMs(){
		return (finishOrderTime.getTime() - placeOrderTime.getTime()) / 1;
	}
	/**
	 * This method defines what an Customer does: The customer attempts to
	 * enter the coffee shop (only successful when the coffee shop has a
	 * free table), place its order, and then leave the coffee shop
	 * when the order is complete.
	 */
	public void run() {
		//YOUR CODE GOES HERE...
		Simulation.logEvent(SimulationEvent.customerStarting(this));

		CoffeeShop coffeeShop = CoffeeShop.getInstance();
		coffeeShop.enterCoffeeShop(this);

		Simulation.logEvent(SimulationEvent.customerEnteredCoffeeShop(this));
		Simulation.logEvent(SimulationEvent.customerPlacedOrder(this, order, orderNum));
		coffeeShop.placeAnOrder(this, orderNum, order);

		//Wait for order to be finished
		synchronized(coffeeShop.getOrderLocked(orderNum)){
			while(coffeeShop.isOrderInProgress(orderNum)){
				try {
					coffeeShop.getOrderLocked(orderNum).wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		// Eating food for a certain time
		try{
			Thread.sleep(eatingTime);
		}catch(InterruptedException e){
			e.printStackTrace();
		}
		// Leave the coffeeShop
		Simulation.logEvent(SimulationEvent.customerReceivedOrder(this, order, orderNum));
		Simulation.logEvent(SimulationEvent.customerLeavingCoffeeShop(this));
		coffeeShop.leaveCoffeeShop(this);
	}
}