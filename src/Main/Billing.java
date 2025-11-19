package Main;

public class Billing {

    public double calculateAmount(double consumption, double ratePerCubic) {
        if (consumption < 0) consumption = 0;
        double amt = consumption * ratePerCubic;
        return Math.round(amt * 100.0) / 100.0;
    }

}
