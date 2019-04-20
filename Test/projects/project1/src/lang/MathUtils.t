@info(
	name="choi",
	time="20190411",
	msg="none"
)
class MathUtils
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
}