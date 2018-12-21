package HW4;

/**
 * @author Bo Han
 */

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Cooks are simulation actors that have at least one field, a name.
 * When running, a cook attempts to retrieve outstanding orders placed
 * by Eaters and process them.
 */
public class Cook implements Runnable {
	private final String name;

	private Map<Food,Machine> machineByFoodType;
	private Map<Integer, Map<Food, List<Thread>>> foodThreadsByOrder;

	public  CoffeeShop coffeeShop;

	/**
	 * You can feel free modify this constructor.  It must
	 * take at least the name, but may take other parameters
	 * if you would find adding them useful. 
	 *
	 * @param: the name of the cook
	 */
	public Cook(String name) {
		this.name = name;
	}

	public Cook(String name, Map<Food, Machine> machinesByFoodType) {
		this.name = name;

		this.machineByFoodType = machinesByFoodType;
		this.foodThreadsByOrder = new HashMap<>();

		coffeeShop = CoffeeShop.getInstance();
	}

	public String toString() {
		return name;
	}

	/**
	 * This method executes as follows.  The cook tries to retrieve
	 * orders placed by Customers.  For each order, a List<Food>, the
	 * cook submits each Food item in the List to an appropriate
	 * Machine, by calling makeFood().  Once all machines have
	 * produced the desired Food, the order is complete, and the Customer
	 * is notified.  The cook can then go to process the next order.
	 * If during its execution the cook is interrupted (i.e., some
	 * other thread calls the interrupt() method on it, which could
	 * raise InterruptedException if the cook is blocking), then it
	 * terminates.
	 */
	public void run() {

		Simulation.logEvent(SimulationEvent.cookStarting(this));

		try {
			while(true) {
				//YOUR CODE GOES HERE...
				int currentOrderNumber;
				synchronized(coffeeShop.getNewOrders()){
					while(!coffeeShop.isHasNewOrders()){
						coffeeShop.getNewOrders().wait();
					}
					currentOrderNumber = coffeeShop.getNextOrderNum();
				}

				//Process the order
				List<Food> order = coffeeShop.getOrder(currentOrderNumber);
				processingOrder(order, currentOrderNumber);
			}
		}
		catch(InterruptedException e) {
			// This code assumes the provided code in the Simulation class
			// that interrupts each cook thread when all customers are done.
			// You might need to change this if you change how things are
			// done in the Simulation class.
			Simulation.logEvent(SimulationEvent.cookEnding(this));
		}
	}

	private void processingOrder(List<Food> order, int orderNum) throws InterruptedException {
		CoffeeShop.startOrderByCustomer.add(orderNum);
		synchronized(coffeeShop.getOrderLocked(orderNum)){

			Simulation.logEvent(SimulationEvent.cookReceivedOrder(this, order, orderNum));
			foodThreadsByOrder.put(orderNum, new HashMap<>());
			Food[] foods = {FoodType.burger,FoodType.fries,FoodType.coffee};

			int[] foodNums = new int[3];
			for(Food food : order){
				if(food.equals(FoodType.burger))
					foodNums[0]++;
				else if(food.equals(FoodType.fries))
					foodNums[1]++;
				else if(food.equals(FoodType.coffee))
					foodNums[2]++;
			}
			for(int i=0; i<3; i++){
				Food food = foods[i];
				foodThreadsByOrder.get(orderNum).put(food, new LinkedList<>());
				for(int j=0; j<foodNums[i]; j++){
					Machine machine = machineByFoodType.get(food);
					Simulation.logEvent(SimulationEvent.cookStartedFood(this, food, orderNum));

					coffeeShop.startOrder(this, orderNum);
					Thread thread = machine.makeFood(food);
					foodThreadsByOrder.get(orderNum).get(food).add(thread);
				}
			}
			for(Food food : foodThreadsByOrder.get(orderNum).keySet()){
				for(Thread thread : foodThreadsByOrder.get(orderNum).get(food)){
					thread.join();
					Simulation.logEvent(SimulationEvent.cookFinishedFood(this, food, orderNum));
				}
			}

			coffeeShop.completeOrder(this, orderNum);
			Simulation.logEvent(SimulationEvent.cookCompletedOrder(this, orderNum));

		}
	}
}