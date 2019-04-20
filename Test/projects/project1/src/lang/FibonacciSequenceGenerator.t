@info(
	name="choi",
	time="20190411",
	msg="none"
)
class FibonacciSequenceGenerator
{
	int n;
	int nn;
	static int nnn;
	int size;
	static int [] generated;
	FibonacciSequenceGenerator(int size)
	{
		this.size = 100;
		this.size = size;
		generated = new int[this.size];
		
		nn = 100;
		nnn = 200;
	}
	int generate(int index)
	{
		int result = size;
				
		if(index == 0)
			result = 0;
		else if(index == 1)
			result = 1;
		else
		{
			if(generated[index-1]!=0)
			{
				result = generated[index-1];
			}else
			{
				result = generate(index - 1) + generate(index - 2);
				generated[index-1] = result;
			}
		}
		return result;
	}	
}