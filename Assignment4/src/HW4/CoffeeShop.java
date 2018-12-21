package HW4;

/**
 * @author Bo Han
 */

import java.util.*;

/**
 * This is the public helper class
 */

/**
 * In general, implementing priorities on customers
 * Priority: ranging from 1 to 3, 1 is the top priority
 * The rule: The higher the priority, the faster this order will be got by the cook.
 * When the priorities are the same, the earlier one will be picked up by the cook.
 */
public class CoffeeShop
{
    private List<Customer> customersList;
    private int totalFinishedOrders;
    private int totalCooks, totalCustomers, machineCapacity, totalTables;
    private boolean randomOrders;
    private static CoffeeShop instance;

    private Set<Integer> newOrders;
    private Map<Integer, List<Food>> ordersByOrderNum;
    private Map<Integer, Object> lockOnOrder;
    private Set<Integer> inProgressOrders;
    private Set<Integer> finishedOrders;
    private Map<Food, Machine> machineByFoodType;

    private Thread[] cooks;
    private Thread[] customers;

    private Object finishedLock;

    // This rand is for generating random priority and random eating time
    private Random rand;

    // This queue is for cook to get the order based on priority and squence
    private PriorityQueue<Node> queue;

    // These are for tracking squences of placing order and processing order
    public static List<Integer> placeOrderByCustomer;
    public static List<Integer> startOrderByCustomer;
    public static Map<Integer,Customer> customerByOrder;

    // This two arrays are for calculating the average waiting time
    public static long[] waitingTimeByPriority;
    public static int[] customerCountByPriority;

    public CoffeeShop(){
        instance = this;

    }

    public static CoffeeShop getInstance(){
        return instance;
    }

    public List<Integer> getPlaceList(){
        return placeOrderByCustomer;
    }

    public List<Integer> getStartList(){
        return startOrderByCustomer;
    }

    public CoffeeShop(int totalCooks, int totalCustomers, int totalTables, int machineCapacity, boolean randomOrders){
        instance = this;
        this.totalCooks =  totalCooks;
        this.totalCustomers = totalCustomers;
        this.totalTables = totalTables;
        this.machineCapacity = machineCapacity;
        this.randomOrders = randomOrders;

        rand = new Random();
        queue = new PriorityQueue<>(100);

        placeOrderByCustomer = new ArrayList<>();
        startOrderByCustomer = new ArrayList<>();

        customerByOrder = new HashMap<>();
        waitingTimeByPriority = new long[4];
        customerCountByPriority = new int[4];
    }

    void runCoffeeShop() {

        totalFinishedOrders = 0;
        customersList = new ArrayList<>(totalTables);
        cooks = new Thread[totalCooks];
        machineByFoodType = new HashMap<>();
        ordersByOrderNum = new HashMap<>();
        lockOnOrder = new HashMap<>();
        newOrders = new HashSet<>();
        inProgressOrders = new HashSet<>();
        finishedOrders = new HashSet<>();
        finishedLock = new Object();

        // Start up machines
        machineByFoodType.put(FoodType.burger, new Machine("Grill", FoodType.burger, machineCapacity));
        machineByFoodType.put(FoodType.fries, new Machine("Frier", FoodType.fries, machineCapacity));
        machineByFoodType.put(FoodType.coffee, new Machine("Star", FoodType.coffee, machineCapacity));

        // Let cooks in
        for (int i = 0; i < totalCooks; i++) {
            cooks[i] = new Thread(new Cook("Cook " + (i), machineByFoodType));
        }

        // Build the customers.
        customers = new Thread[totalCustomers];
        LinkedList<Food> order;
        if (!randomOrders) {
            order = new LinkedList<>();
            order.add(FoodType.burger);
            order.add(FoodType.burger);
            order.add(FoodType.fries);
            order.add(FoodType.fries);
            order.add(FoodType.coffee);

            for (int i = 0; i < customers.length; i++) {
                customers[i] = new Thread(new Customer("Customer " + (i), order, rand.nextInt(3)+1, (long)(rand.nextInt(2000)+1)));
            }
        }
        else {
            for (int i = 0; i < customers.length; i++) {
                Random random = new Random();
                int burgerNum = random.nextInt(4);
                int friesNum = random.nextInt(4);
                int coffeeNum = random.nextInt(4);

                order = new LinkedList<>();
                for (int b = 0; b < burgerNum; b++) {
                    order.add(FoodType.burger);
                }
                for (int f = 0; f < friesNum; f++) {
                    order.add(FoodType.fries);
                }
                for (int c = 0; c < coffeeNum; c++) {
                    order.add(FoodType.coffee);
                }
                customers[i] = new Thread(new Customer("Customer " + (i), order, rand.nextInt(3)+1, (long)(rand.nextInt(2000)+1)));
            }
        }

        for (Thread cook : cooks) {
            cook.start();
        }

        // Now "let the customers know the shop is open" by
        // starting them running in their own thread.
        for (Thread customer : customers) {
            //System.out.println(customers.length);
            customer.start();
            // NOTE: Starting the customer does NOT mean they get to go
            // right into the shop. There has to be a table for
            // them. The Customer class' run method has many jobs
            // to do - one of these is waiting for an available
            // table...
        }

        try {
            // Wait for customers to finish
            // -- you need to add some code here...
            for (Thread customer : customers) {
                customer.join();
            }

            while (!areAllOrdersFinished()) {
                synchronized (finishedLock) {
                    try {
                        finishedLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            // Then send cooks home...
            // The easiest way to do this might be the following, where
            // we interrupt their threads. There are other approaches
            // though, so you can change this if you want to.
            for (Thread cook : cooks) {
                cook.interrupt();
            }
            for (Thread cook : cooks) {
                cook.join();
            }

        } catch (InterruptedException e) {
            System.out.println("Simulation thread interrupted.");
        }

        // Shut down machines
        Simulation.logEvent(SimulationEvent.machineEnding(machineByFoodType.remove(FoodType.burger)));
        Simulation.logEvent(SimulationEvent.machineEnding(machineByFoodType.remove(FoodType.fries)));
        Simulation.logEvent(SimulationEvent.machineEnding(machineByFoodType.remove(FoodType.coffee)));
    }

    public void enterCoffeeShop(Customer customer){
        synchronized (customersList){
            while(customersList.size() >= totalTables){
                try{
                    customersList.wait();
                }catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
            customersList.add(customer);
        }
    }


    public void leaveCoffeeShop(Customer customer){
        synchronized (customersList) {
            customersList.remove(customer);
            customersList.notifyAll();
        }
    }

    public boolean placeAnOrder(Customer customer,int orderNumber,List<Food> order){
        if (customer == null || order == null || newOrders == null || ordersByOrderNum == null) {
            return false;
        }
        synchronized (ordersByOrderNum) {
            ordersByOrderNum.put(orderNumber, order);
        }

        synchronized (lockOnOrder) {
            lockOnOrder.put(orderNumber, new Object());
        }


        synchronized (queue) {
            Node node = new Node(customer.getPriority(),orderNumber);
            queue.add(node);
            placeOrderByCustomer.add(orderNumber);
            customer.setPlaceTime();
            synchronized (this) {
                this.notifyAll();
            }
            queue.notifyAll();
        }
        return true;
    }

    public void startOrder(Cook cook, int orderNum) {
        synchronized (getOrderLocked(orderNum)) {
            synchronized (inProgressOrders) {
                inProgressOrders.add(orderNum);
                getOrderLocked(orderNum).notifyAll();
            }
        }
    }

    private boolean areAllOrdersFinished() {
        synchronized (finishedLock) {
            return totalFinishedOrders == totalCustomers;
        }
    }

    public List<Food> getOrder(int orderNum){
        synchronized (ordersByOrderNum) {
            return ordersByOrderNum.get(orderNum);
        }
    }

    public boolean isOrderInProgress(int orderNum){
        synchronized (getOrderLocked(orderNum)) {
            synchronized (finishedOrders) {
                return !finishedOrders.contains(orderNum);
            }
        }
    }

    public boolean isHasNewOrders(){
        synchronized (queue) {
            return !queue.isEmpty();
        }
    }


    public int getNextOrderNum(){
        synchronized (queue) {
            while (queue.isEmpty() && !areAllOrdersFinished()) {
                if (areAllOrdersFinished()) {
                    return -1;
                }
                try {
                    queue.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (areAllOrdersFinished()) {
                    return -1;
                }
            }

            Node node = queue.poll();
            return node.getOrder();
        }
    }

    public Object getOrderLocked(int orderNum){
        synchronized (lockOnOrder) {
            return lockOnOrder.get(orderNum);
        }
    }

    public void completeOrder(Cook cook, int orderNum) {
        synchronized (getOrderLocked(orderNum)) {
            synchronized (inProgressOrders) {
                inProgressOrders.remove(orderNum);
                synchronized (finishedOrders) {
                    finishedOrders.add(orderNum);
                    Customer customer = customerByOrder.get(orderNum);
                    customer.setFinishTime();
                    int customerPriority = customer.getPriority();
                    long waitingTime = customer.getWaitingTimeByMs();
                    customerCountByPriority[customerPriority]++;
                    waitingTimeByPriority[customerPriority] += waitingTime;
                    System.out.println(customer.getName()+" waiting time is: " + waitingTime + "ms, Priority: " + customerPriority);
                }
                synchronized (finishedLock) {
                    totalFinishedOrders++;
                }
            }
            getOrderLocked(orderNum).notifyAll();
        }
    }


    public PriorityQueue<Node> getNewOrders() {
        synchronized (queue) {
            return queue;
        }
    }

    private class Node implements Comparable<Node>{
        private int customerPriority;
        private int orderNum;

        public Node(int customerPriority, int orderNum){
            this.customerPriority = customerPriority;
            this.orderNum = orderNum;
        }

        public int getPriority(){
            return customerPriority;
        }

        public int getOrder(){
            return orderNum;
        }
        @Override
        public int compareTo(Node o) {
            if(this.getPriority() >= o.getPriority())
                return 1;
            else
                return 0;
        }
    }

}
