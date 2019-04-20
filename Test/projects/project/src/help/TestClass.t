import tiki.util.Math;
import Test;
@info(
	name="choi",
	time="20160523",
	msg="none"
)
class TestClass
{
	int id;
	boolean initialized;
	static int numberOfClass;
	
	TestClass next;
	int [] result;
	TestClass getNext()
	{
		return this.next;
	}
	int getX()
	{
		help.Ti t = new Ti();
		t.show();
		return 100;
	}	
	static int sum;
	void show()
	{
		System.print("result:");
		for(int i=0;i<result.length;i++)
		{
			System.print("%5d ",this.result[i]);
		}
		System.print("\n");
	}
	void qsort(int []a, int low, int high)
	{
		if(low >= high)
		{
			return;
		}
		int first = low;
		int last = high;
		int key = a[first];
		
		while(first < last)
		{
			while(first < last && a[last] >= key)
			{
				last--;
			}
	 
			a[first] = a[last];
	 
			while(first < last && a[first] <= key)
			{
				first++;
				
				first--;
				--first;
				--first;
				
				first = first + 2;
				++first;
			}
			 
			a[last] = a[first];    
		}
		a[first] = key;
		qsort(a, low, first-1);
		this.qsort(a, first+1, high);
	}
	static int [] tmp;
	static int FibonacciSequence(int index)
	{
		int result;
				
		if(index == 0)
			result = 0;
		else if(index == 1)
			result = 1;
		else
		{
			if(tmp[index-1]!=0)
			{
				result = tmp[index-1];
			}else
			{
				result = FibonacciSequence(index - 1) + TestClass.FibonacciSequence(index - 2);
				tmp[index-1] = result;
			}
		}
		return result;
	}	
	TestClass(Test u,int size,int max)
	{
		System.print("TestClass.initialized = %s\n",initialized);
		id = this.numberOfClass++;
		
		result = new int[size];
		
		for(int i=0;i<size;i++)
		{
			this.result[i] = Math.random(0,max);
			
			sum = sum + this.result[i];
		}		
		
		Test.size++;
		Test.test001();
		Test.size--;
	}
	static boolean used;
}