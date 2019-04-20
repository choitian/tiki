@info(
	name="choi",
	time="20160523",
	msg="none"
)
class Math
{
	static void qsort(int []a, int low, int high)
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
		Math.qsort(a, first+1, high);
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
				result = FibonacciSequence(index - 1) + FibonacciSequence(index - 2);
				tmp[index-1] = result;
			}
		}
		return result;
	}	
}