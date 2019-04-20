package tiki.syntax.node;

import java.util.LinkedList;

import org.w3c.dom.Element;

import tiki.syntax.Analyzer;
import tiki.syntax.SymbolManager;

public class ListNode extends SyntaxNode {
	ListNode list;
	
	SyntaxNode node;

	public ListNode(){}
	public ListNode(ListNode list,SyntaxNode node){
		this.list = list;
		this.node =node;
		
		if(this.list !=null)
		{
			this.setTree(this.list.getTree());
			this.setPosition(this.list.getPosition());
		}else if(this.node !=null)
		{
			this.setTree(this.node.getTree());
			this.setPosition(this.node.getPosition());
		}
	}	
	public ListNode(SyntaxNode node0,SyntaxNode node1){
		this.list = new ListNode(null,node0);
		this.node =node1;
		
		if(this.list !=null)
		{
			this.setTree(this.list.getTree());
			this.setPosition(this.list.getPosition());
		}else if(this.node !=null)
		{
			this.setTree(this.node.getTree());
			this.setPosition(this.node.getPosition());
		}
	}
	@Override
	public void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
		for (SyntaxNode node : nodes()) {
			node.check();
		}
	}

	public LinkedList<SyntaxNode> nodes() {
		LinkedList<SyntaxNode> ret = new LinkedList<SyntaxNode>();
		walk(ret);
		return ret;
	}

	public void setList(ListNode list) {
		this.list = list;
	}

	public void setNode(SyntaxNode node) {
		this.node = node;
	}

	@Override
	public void toXML(Element upper) {
		for (SyntaxNode node : nodes()) {
			node.toXML(upper);
		}
	}

	private void walk(LinkedList<SyntaxNode> nodes) {
		if (list != null) {
			list.walk(nodes);
		}
		if (node != null) {
			nodes.add(node);
		}
	}
}
