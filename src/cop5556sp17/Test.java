package cop5556sp17;

public class Test {

int x;
static int y;


int sumXY(int val) throws InterruptedException{
  x = val;
  while(x > 1){
	  int j = 0;
	  j++;
	  while( j < 2){
		  int i=2;
		  i++;
		  x=i;
	  }
  }
  return val;
}

}

