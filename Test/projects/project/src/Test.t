import help.TestClass;
class Test
{
	//1,recursion Methods/library Methods/construct Methods,varargs Method.
	//2,new array/class,array of class.
	//3,class member: Variables,Methods,class Variables,Methods return class.
	//4,static class members:Variables,Methods
	//5,this or non-this access
	static void showNext(TestClass start )
	{
		int id = start.next.getNext().next.id;
		System.print("start.next.getNext().next.id: %d\n",id);
		
		help.TestClass tmp = start.next.next;
		//start.next.next = null;
		System.print("start.getNext().next.getX(): %d\n",start.getNext().next.getX());
		
		start.next.next = tmp;
	}
	static void testA(int size,int ... maxs)
	{	
		System.print("\n***testA***\n"); 
		
		help.TestClass [] tcs = new TestClass[maxs.length];		
		for(int i=0;i<tcs.length;i=i+1)
		{
			tcs[i] = new help.TestClass(null,size,maxs[i]);
			if(i % 2 == 0)
				tcs[i].qsort(tcs[i].result,0,tcs[i].result.length - 1);
			else
				Math.qsort(tcs[i].result,0,tcs[i].result.length - 1);
			if(i != 0)
			{
				tcs[i-1].next = tcs[i];
			}
		}
		tcs[tcs.length-1].next = tcs[0];
		System.print("help.TestClass.numberOfClass: %d\n",help.TestClass.numberOfClass);		
		
		help.TestClass start = tcs[tcs.length/3];
		help.TestClass cur = start;
		showNext(start);
		while(true)
		{
			System.print("cur(%d):",cur.id);
			cur.show();
			
			cur = cur.getNext();
			if(cur==start)
				break;	
		}		
		System.print("sum: %d\n",help.TestClass.sum);
		
		
			
		int snd = 3;
		int thd = 5;
		int [][][]martix = new int[2][snd][thd];
		
		for(int i=0;i<martix.length;i=i+1)
		{
			for(int j=0;j<martix[0].length;j++)
			{
				for(int k=0;k<martix[0][0].length;k++)
				{
					martix[i][j][k] = (i+1)*100 + (j+1)*10 + (k+1);
				}
			}
		} 
		
		int i = 0;
		for(;;i++)
		{
			if(i >= martix.length)
				break;
			for(int j=0;;)
			{
				if(!(j<martix[0].length))
					break;				
				for(int k=0;k<martix[0][0].length;)
				{
					System.print("%03d ",martix[i][j][k]);
					
					k = k + 1;
				}
				j++;		
			}
		} 
		System.print("\n");			
		
		int iv ;
		float fv;
		boolean bv;
		System.print("%d,%f,%b\n",iv,fv,bv);
		
		float PI = 3.14;
		int scale = 7 + (int)PI;
		
		int fsize = Test.size + 41;
		fsize = fsize - Test.size;
		help.TestClass.tmp = new int[fsize];

		
		System.print("-PI * %d: %05.2f,FibonacciSequence[%d]: %d\n",scale,(Object)((-PI) * scale),fsize,help.TestClass.FibonacciSequence(fsize));
		
		String str = "abcdefg";
		System.print("%s,%d  10 %% 3: %d\n",str + 100,str.length(),10 % 3);	
		
		
		str = null;
		boolean bbb = str==null;
		
		help.TestClass next =  start.getNext();
		System.print("%b %b",next==start,next==start.next);	
		
		int []aa = {3,6,9};
		int [][][]martix2 ={
				{{1,2,3,4,5,},aa,{},},
				{{1,2,3,4,5},{,},{1,}},
			};
		for(int i=0;i<martix2.length;i=i+1)
		{
			for(int j=0;j<martix2[i].length;j++)
			{
				for(int k=0;k<martix2[i][j].length;k++)
				{
					System.print("martix2[%d][%d][%d] = %d\n",i,j,k,martix2[i][j][k]);
				}
			}
		} 
	}
	static void  test001()
	{
		System.print("in Test.test001: size %d\n",size);
	}
	static int size;
	static int  main()
	{
		int i;
		Test.size  = 17;
		testA(size,1000,2000,3000,4000);
		
		System.print("\n");
		
		System.print("now: %s\n",Date.now("yyyy-MM-dd hh:mm:ss"));
	}
}