package cop5556sp17;

public class Test {

int x;
static int y;


int sumXY(int val) throws InterruptedException{
  x = val;
  java.lang.Thread.sleep(val);
  return val;
}

}

