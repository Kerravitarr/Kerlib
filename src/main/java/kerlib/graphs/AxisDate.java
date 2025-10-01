package kerlib.graphs;

import java.util.Date;

public class AxisDate extends Axis<Date>{
    public AxisDate(String name, String unit) {
        super(name, unit);
    }
    @Override
    double transform(Date v) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'transform'");
    }    
}
