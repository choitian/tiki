import lang.MathUtils;
import lang.FibonacciSequenceGenerator;
class Test
{
	static void  test__qsort()
	{
		int [] result;
		int size = 10;
		result = new int[size];
		
		//initialize
		for(int i=0;i<size;i++)
		{
			result[i] = Math.random(0,9999);
		}
		
		//show initialized values
		System.print("result initialized:");
		for(int i=0;i<result.length;i++)
			System.print("%5d ",result[i]);
		System.print("\n");
		
		//sort them
		MathUtils.qsort(result,0,result.length - 1);
		
		//show after sorted values
		System.print("result initialized:");
		for(int i=0;i<result.length;i++)
		{
			System.print("%5d ",result[i]);
		}
		System.print("\n");
	}
	static void  test__FibonacciSequence()
	{
		//Fibonacci numbers is: 0 1 1 2 3 5 8 13 21 34 55 89 144 233 377
		int [] result = new int[15];
		
		FibonacciSequenceGenerator fsg = new FibonacciSequenceGenerator(result.length);
		System.print("fsg.nn: %5d\n",fsg.nn);
		System.print("fsg.nn: %5d\n",fsg.nnn);
		System.print("fsg.nn: %5d\n",FibonacciSequenceGenerator.nnn);
		//initialize
		for(int i=0;i<result.length;i++)
		{
			result[i] = fsg.generate(i);
		}
		
		//show initialized values
		System.print("result initialized:");
		for(int i=0;i<result.length;i++)
		{
			System.print("%5d ",result[i]);
		}
		System.print("\n");
	}	
	static void  test__PI()
	{
		//PI = 4/1 - 4/3 + 4/5 - 4/7 + 4/9 - 4/11 + 4/13 -...
		float PI = 4;
		boolean plus = false;
		for (int i = 3; i < 99999; i = i + 2)
		{
		    if(plus)
		    {
		        PI = PI + 4.0 / i;
		    }
		    else
		    {
		        PI = PI - 4.0 / i;
		    }
		    plus = !plus;
		 }
		System.print("%f\n",PI);
	}	
	static void  test__MultidimensionalArrayA()
	{
		int []dimensional_1 = {3,6,9};
		int [][][]dimensional_3 =
			{
				{
					{1,2,3,4,5,},
					dimensional_1,
				},
				{
					{1,2,3,4,5},
					{,},
					{1,1,1}
				},
			};
		for(int i=0;i<dimensional_3.length;i++)
		{
			for(int j=0;j<dimensional_3[i].length;j++)
			{
				for(int k=0;k<dimensional_3[i][j].length;k++)
				{
					System.print("dimensional_3[%d][%d][%d] = %d\n",i,j,k,dimensional_3[i][j][k]);
				}
			}
		} 
	}
	static void  test__MultidimensionalArrayB()
	{
		//int [][][]dimensional_3 = new int[2][3][4];
		int [][][]dimensional_3;
		
		dimensional_3 = new Object[2];
		for(int i=0;i<dimensional_3.length;i++)
		{
			dimensional_3[i]= new Object[3];
			for(int j=0;j<dimensional_3[i].length;j++)
			{
				dimensional_3[i][j] = new int[4];
			}			
		}
		
		for(int i=0;i<dimensional_3.length;i++)
		{
			for(int j=0;j<dimensional_3[i].length;j++)
			{
				for(int k=0;k<dimensional_3[i][j].length;k++)
				{
					System.print("dimensional_3[%d][%d][%d] = %d\n",i,j,k,dimensional_3[i][j][k]);
				}
			}
		} 
	}
	static void test__VarargA(int size,int ... maxs)
	{	
		System.print("size = %d\n",size);
		
		for(int i=0,size = 777 ;i<maxs.length;i++,size++)
		{
			System.print("maxs[%d] = %d\n",i ,maxs[i]);
		} 
		System.print("size = %d\n",size);
	}
	static void test__VarargB(int ... maxs)
	{	
		for(int i=0;i<maxs.length;i++)
		{
			System.print("maxs[%d] = %d\n",i ,maxs[i]);
		} 
	}
	static int  main()
	{
		test__qsort();
		test__FibonacciSequence();
		test__PI();
		test__MultidimensionalArrayA();
		test__MultidimensionalArrayB();
		test__VarargA(666,1,3,5,7,11);
		test__VarargB(666,1,3,5,7,11);
	}
}