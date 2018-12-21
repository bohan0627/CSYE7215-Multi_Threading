package hw2;

/**
 *  @author Bo Han(NUID: 001815357)
 */

import java.util.List;

/**
 * Class provided for ease of test. This will not be used in the project
 * evaluation, so feel free to modify it as you like.
 */
public class Simulation
{
    public static void main(String[] args)
    {
        int nrSellers = 800;
        int nrBidders = 800;

        //AuctionServer server = AuctionServer.getInstance();
        Thread[] sellerThreads = new Thread[nrSellers];
        Thread[] bidderThreads = new Thread[nrBidders];
        Seller[] sellers = new Seller[nrSellers];
        Bidder[] bidders = new Bidder[nrBidders];

        // Start the sellers
        for (int i=0; i<nrSellers; ++i)
        {
            sellers[i] = new Seller(
                    AuctionServer.getInstance(),
                    "Seller"+i,
                    100, 50, i
            );
            sellerThreads[i] = new Thread(sellers[i]);
            sellerThreads[i].start();
        }
        AuctionServer.getInstance().setTime();
        // Start the buyers
        for (int i=0; i<nrBidders; ++i)
        {
            bidders[i] = new Bidder(
                    AuctionServer.getInstance(),
                    "Buyer"+i,
                    1000, 100, 150, i
            );
            bidderThreads[i] = new Thread(bidders[i]);
            bidderThreads[i].start();
        }



        // Join on the sellers
        for (int i=0; i<nrSellers; ++i)
        {
            try
            {
                sellerThreads[i].join();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        // Join on the bidders
        for (int i=0; i<nrBidders; ++i)
        {
            try
            {
                bidderThreads[i].join();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }



        // TODO: Add code as needed to debug

        // View auction server operating status
        System.out.println("This auction server operating status is listed as below:");
        System.out.println("Total revenue: " + AuctionServer.getInstance().auctionServerRevenue());
        System.out.println("Total bidders: " + bidders.length + ", " + "Participants: " + AuctionServer.getInstance().participatedBidders());
        System.out.println("Total sellers: " + sellers.length + ", " + "Participants: " + AuctionServer.getInstance().participatedSellers());
        System.out.println("Total submitted items:" + AuctionServer.getInstance().totalSubmittedItems());
        System.out.println("Total sold items:" + AuctionServer.getInstance().totalSoldItems());
        System.out.println("Items selling rate:" + AuctionServer.getInstance().itemsSellingRate());


        // Check whether this auction server operates normally: Total spent by all the bidders should be equal to auction server avenue
        int totalCashSpentByBidders = 0;
        for(Bidder bidder: bidders)
            totalCashSpentByBidders += bidder.cashSpent();
        if (totalCashSpentByBidders == AuctionServer.getInstance().auctionServerRevenue()){
            System.out.println("This auction server is operating normally");
        }

        else{
            System.out.println("Some defects in this auction server");
        }


        // Check the bias policy has been actually implemented
        List<String> biasStatus = AuctionServer.getInstance().biasCheck;
        if(biasStatus==null)
            System.out.println("Bias Policy not work");
        else{
            System.out.println("Check bias policy working status");
            for(String s: biasStatus)
                System.out.println(s);
        }

        /*
        // View each bidder transaction details
        int bidderTransactions;
        for (Bidder b : bidders){
            bidderTransactions = AuctionServer.getInstance().bidderCashSpent(b.name());
            System.out.println("The total cash spent by " + b.name() + " is: " + bidderTransactions);
        }
        // View the highest bid for each item
        AuctionServer.getInstance().highestBid();
        */
    }

}
