package tiki.tools.complier.lalr;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tiki.uitls.collection.HashSet;
import tiki.uitls.collection.IHash;
import tiki.uitls.collection.Pair;

class ItemLR0 implements IHash{
	public Production production;
	public int dot;

	public ItemLR0() {
		production = null;
		dot = 0;
	}

	public Symbol DotRight() {
		if (EndWithDot())
			return null;

		return production.nodes.get(dot);
	}

	public boolean EndWithDot() {
		return BNF.isNULLProduction(production) || dot >= production.NodeSize();
	}

	@Override
	public String hashString() {
		return Integer.toString(production.id) + "." + Integer.toString(dot);
	}

	@Override
	public String toString() {
		return production.ToText() + '/' + Integer.toString(dot);
	}
}
class ItemLR1 implements IHash{
	public ItemLR0 core;

	public Symbol lookahead;

	public ItemLR1() {
		core = null;
		lookahead = null;
	}

	@Override
	public String hashString() {
		return core.hashString() + "." + Integer.toString(lookahead.id);
	}

	@Override
	public String toString() {
		return core.toString() + '/' + lookahead.text;
	}
}
public class LALR {
	BNF bnf_;

	ItemLR0 item_accept_;
	ItemLR0 item_initial_;
	public HashSet<ItemLR0> item_lr0_set;

	public HashSet<ItemLR1> item_lr1_set;
	State state_initial_;
	public HashSet<State> state_set;

	public LALR() {
		bnf_ = null;
		item_initial_ = null;
		item_accept_ = null;
		state_initial_ = null;

		item_lr0_set = new HashSet<>();
		item_lr1_set = new HashSet<>();
		state_set = new HashSet<>();
	}

	void AddToUncheckedSymbol(ItemLR0 item, Stack<Symbol> uncheckeds, TreeSet<Integer> checkeds) {
		Symbol dot_right = item.DotRight();
		if (dot_right != null && !dot_right.isTerminal() && !checkeds.contains(dot_right.id)) {
			uncheckeds.push(dot_right);

			checkeds.add(dot_right.id);
		}
	}

	void AddToUncheckedSymbolAndLookahead(ItemLR1 item, Stack<Pair<Symbol, Symbol>> uncheckeds,
			TreeSet<String> checkeds) {
		ItemLR0 core = item.core;
		Symbol lookahead = item.lookahead;

		Symbol dot_right = core.DotRight();
		if (!BNF.isNULLProduction(core.production) && dot_right != null && !dot_right.isTerminal()) {
			for (int index = core.dot + 1;; index++) {
				/*
				 * core.production can never be H --> __NULL__.
				 */
				if (index >= core.production.NodeSize()) {
					String key = dot_right.text + "." + lookahead.text;
					if (checkeds.add(key)) {
						Pair<Symbol, Symbol> symbol_and_lookahead = new Pair<Symbol, Symbol>(dot_right, lookahead);
						uncheckeds.push(symbol_and_lookahead);
					}
					break;
				}

				Symbol node = core.production.nodes.get(index);

				String key = dot_right.text + "." + node.text;
				if (checkeds.add(key)) {
					if (node.isTerminal()) {
						Pair<Symbol, Symbol> symbol_and_lookahead = new Pair<Symbol, Symbol>(dot_right, node);
						uncheckeds.push(symbol_and_lookahead);
					} else {
						for (String fst : node.first_set) {
							String key_fst = dot_right.text + "." + fst;
							if (checkeds.add(key_fst)) {
								Symbol symbol = bnf_.symbol_map_.get(fst);
								Pair<Symbol, Symbol> symbol_and_lookahead = new Pair<Symbol, Symbol>(dot_right, symbol);

								uncheckeds.push(symbol_and_lookahead);
							}
						}
					}
				}
				if (!node.nullable)
					break;
			}
		}
	}

	public void build(InputStream bnf) throws Exception {
		bnf_ = new BNF(bnf);

		long start = System.currentTimeMillis();
		bnf_.Setup();
		Logger.getGlobal().info("Setup               \t\t\t" + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		BuildAutomatonLR0();
		Logger.getGlobal().info("BuildAutomatonLR0   \t\t\t" + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		do_propagation();
		Logger.getGlobal().info("do_propagation       \t\t\t" + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		ConstructParsingTable();
		Logger.getGlobal().info("ConstructParsingTable\t\t\t" + (System.currentTimeMillis() - start));
	}

	void BuildAutomatonLR0() {
		item_initial_ = TryMakeItemLR0(bnf_.production_start_, 0);
		item_accept_ = TryMakeItemLR0IncDot(item_initial_);

		state_initial_ = new State();
		state_initial_.item_lr0_set_kernel.add(item_initial_);
		state_initial_.item_lr0_set_all.add(item_initial_);

		TryAddState(state_initial_);

		LinkedList<State> uncheckeds = new LinkedList<State>();
		uncheckeds.add(state_initial_);

		LinkedHashMap<String, State> goto_catalogue = new LinkedHashMap<String, State>();
		while (!uncheckeds.isEmpty()) {
			State unchecked = uncheckeds.remove();

			ClosureLR0(unchecked);
			CatalogueGOTO(unchecked, goto_catalogue);

			for (Entry<String, State> entry : goto_catalogue.entrySet()) {
				String on = entry.getKey();
				State to = entry.getValue();
				Pair<State, Boolean> result = TryAddState(to);
				if (result.second) {
					uncheckeds.add(to);
				}
				Symbol s = bnf_.symbol_map_.get(on);
				unchecked.goto_table.put(s.text, result.first);
			}
		}

		{
			/*
			System.out.print(String.format("state: %d\n",state_map_.size()));
			int kernelSum = 0;
			int gotoSum = 0;
			for (Entry<String, State> entry : state_map_.entrySet()) {
				State state = entry.getValue();
				
				kernelSum+=state.item_lr0_set_kernel.size();
				gotoSum+=state.goto_table.size();
				
			}
			System.out.print(String.format("kernelSum %d,gotoSum %d\n",kernelSum,gotoSum));
			for (Entry<String, State> entry : state_map_.entrySet()) {
				State state = entry.getValue();				
				System.out.print(String.format("state:%d\n",state.id));
				for(ItemLR0 kernel:state.item_lr0_set_kernel)
				{
					System.out.print(String.format("%s\n",kernel.toString()));
				}	
				
				System.out.print(String.format("\n\n\n"));
			}			
			for (Entry<String, State> entry : state_map_.entrySet()) {
				State state = entry.getValue();				
				for(ItemLR0 kernel:state.item_lr0_set_kernel)
				{
					
					for (Entry<String, State> entry1 : state_map_.entrySet()) {
						State state1 = entry1.getValue();
						if(state1==state)
							continue;
						
						for(ItemLR0 kernel1:state1.item_lr0_set_kernel)
						{
							if(kernel.hashString().equals(kernel1.hashString()))
							{
								System.out.print(String.format("%d and %d,has same kernel %s\n",state.id,state1.id,kernel.toString()));
							}
						}
					}	
				}
				
			}
			*/
		}
	}

	void BuildPropagateAndSpontaneouTable(State state, ItemLR0 kernel,
			LinkedHashMap<String, TreeSet<String>> propagate_table, LinkedList<Pair<State, ItemLR1>> unpropagateds) {
		State test_state = new State();
		test_state.item_lr1_set_all.add(TryMakeItemLR1(kernel, bnf_.symbol_end_));

		ClosureLR1(test_state);
		TreeSet<String> propagate_table_item = new TreeSet<String>();
		for (ItemLR1 lr1 : test_state.item_lr1_set_all.set()) {
			if (!lr1.core.EndWithDot()) {
				if (lr1.lookahead != bnf_.symbol_end_) {
				
					State to = state.goto_table.get(lr1.core.DotRight().text);
					SendTo(state,to, lr1.core, lr1.lookahead, unpropagateds);
				} else {
					propagate_table_item.add(lr1.core.hashString());
				}
			}
		}

		if (!propagate_table.containsKey(kernel.hashString())) {
			propagate_table.put(kernel.hashString(), propagate_table_item);
		}
	}

	void CatalogueGOTO(State state, LinkedHashMap<String, State> catalogue) {
		catalogue.clear();
		for (ItemLR0 item : state.item_lr0_set_all.set()) {
			Symbol dot_right = item.DotRight();
			if (dot_right != null) {
				State new_state = catalogue.get(dot_right.text);
				if (new_state == null) {
					new_state = new State();
					catalogue.put(dot_right.text, new_state);
				}

				ItemLR0 new_item = TryMakeItemLR0IncDot(item);
				new_state.item_lr0_set_all.add(new_item);
				new_state.item_lr0_set_kernel.add(new_item);
			}
		}
	}

	void ClosureLR0(State state) {
		Stack<Symbol> uncheckeds = new Stack<Symbol>();
		TreeSet<Integer> checkeds = new TreeSet<Integer>();

		for (ItemLR0 item : state.item_lr0_set_all.set()) {
			AddToUncheckedSymbol(item, uncheckeds, checkeds);
		}

		while (!uncheckeds.isEmpty()) {
			Symbol unchecked = uncheckeds.pop();

			for (Production production : unchecked.productions) {
				ItemLR0 new_item = TryMakeItemLR0(production, 0);
				if (state.item_lr0_set_all.add(new_item)) {
					AddToUncheckedSymbol(new_item, uncheckeds, checkeds);
				}
			}
		}
	}

	void ClosureLR1(State state) {
		Stack<Pair<Symbol, Symbol>> uncheckeds = new Stack<Pair<Symbol, Symbol>>();
		TreeSet<String> checkeds = new TreeSet<String>();

		for (ItemLR1 item : state.item_lr1_set_all.set()) {
			AddToUncheckedSymbolAndLookahead(item, uncheckeds, checkeds);
		}

		while (!uncheckeds.isEmpty()) {
			Pair<Symbol, Symbol> entry = uncheckeds.pop();
			Symbol non_terminal = entry.first;
			Symbol lookahead = entry.second;

			for (Production production : non_terminal.productions) {
				ItemLR1 new_item = TryMakeItemLR1(production, 0, lookahead);

				if (state.item_lr1_set_all.add(new_item)) {
					AddToUncheckedSymbolAndLookahead(new_item, uncheckeds, checkeds);
				}
			}
		}
	}

	void ConstructParsingTable() throws Exception {
		for (State state : state_set.set()) {
			ClosureLR1(state);

			for (ItemLR1 item : state.item_lr1_set_all.set()) {
				if (item.core.EndWithDot()) {
					if (item.core == item_accept_) {
						state.parsing_table.put(item.lookahead.text, "accept");
					} else {
						if (state.parsing_table.containsKey(item.lookahead.text)) {
							String action = state.parsing_table.get(item.lookahead.text);
							if (action.startsWith("reduce")) {
								Production byold = bnf_.getProductions().get(Integer.parseInt(action.substring(6)));
								Production byNew = item.core.production;

								String msg = String.format("Error,Conflicting(R/R) On: %s\n%s\n%s", item.lookahead.text,
										byold.ToText(), byNew.ToText());
								throw new Exception(msg);
							} else if (action.startsWith("shift")) {
								Logger.getGlobal()
										.info("Conflicting(S/R) On:" + item.lookahead.text + ",Perfer Shift");
							}
						} else {
							state.parsing_table.put(item.lookahead.text, "reduce" + item.core.production.id);
						}
					}
				} else {
					Symbol dot_right = item.core.DotRight();
					if (dot_right.isTerminal()) {
						if (state.parsing_table.containsKey(dot_right.text)) {
							String action = state.parsing_table.get(dot_right.text);
							if (action.startsWith("reduce")) {
								Logger.getGlobal().info("Conflicting(S/R) On:" + dot_right.text
										+ ",Perfer Shift Over Reduce " + state.parsing_table.get(dot_right.text));

								Production prod = bnf_.getProductions().get(Integer.parseInt(action.substring(6)));
								Logger.getGlobal().info("Production:" + prod.ToText());
								
								state.parsing_table.put(dot_right.text, "shift");
							}
						}else {
							state.parsing_table.put(dot_right.text, "shift");
						}
						
					}
				}
			}

		}
	}

	void DiscoveringPropagatedAndSpontaneousLookaheads(LinkedList<Pair<State, ItemLR1>> unpropagateds,
			LinkedHashMap<String, TreeSet<String>> propagate_table) {
		ItemLR1 item = TryMakeItemLR1(item_initial_, bnf_.symbol_end_);
		state_initial_.item_lr1_set_all.add(item);
		state_initial_.item_lr1_set_kernel.add(item);
		unpropagateds.add(new Pair<State, ItemLR1>(state_initial_, item));

		for (State state : state_set.set()) {
			for (ItemLR0 kernel : state.item_lr0_set_kernel.set()) {
				BuildPropagateAndSpontaneouTable(state, kernel, propagate_table, unpropagateds);
			}
		}
	}

	void do_propagation() {
		LinkedList<Pair<State, ItemLR1>> unpropagateds = new LinkedList<Pair<State, ItemLR1>>();
		LinkedHashMap<String, TreeSet<String>> propagate_table = new LinkedHashMap<String, TreeSet<String>>();

		DiscoveringPropagatedAndSpontaneousLookaheads(unpropagateds, propagate_table);
		while (!unpropagateds.isEmpty()) {
			Pair<State, ItemLR1> entry = unpropagateds.remove();
			State from_state = entry.first;
			ItemLR1 from_item = entry.second;
			TreeSet<String> propagate_table_item = propagate_table.get(from_item.core.hashString());
			if (propagate_table_item != null) {
				for (String by_core_hash_string : propagate_table_item) {
					ItemLR0 by_core = item_lr0_set.get(by_core_hash_string);
					
					State to = from_state.goto_table.get(by_core.DotRight().text);
					SendTo(from_state, to,by_core, from_item.lookahead, unpropagateds);
				}
			}
		}
	}

	void SendTo(State from,State to, ItemLR0 by_core, Symbol lookahead, LinkedList<Pair<State, ItemLR1>> unpropagateds) {
		Production production = by_core.production;
		int dot = by_core.dot + 1;

		ItemLR1 new_item = TryMakeItemLR1(production, dot, lookahead);
		if (to.item_lr1_set_kernel.add(new_item)) {
			to.item_lr1_set_all.add(new_item);
			unpropagateds.add(new Pair<State, ItemLR1>(to, new_item));
		}
	}

	public Document toXML() {
		try {
			DocumentBuilderFactory doc_builder_factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder doc_builder = doc_builder_factory.newDocumentBuilder();
			Document doc = doc_builder.newDocument();
			Element root = doc.createElement("root");
			doc.appendChild(root);

			for (Production production : bnf_.getProductions()) {
				Element production_e = doc.createElement("production");

				Attr attr;

				attr = doc.createAttribute("id");
				attr.setValue(Integer.toString(production.id));
				production_e.setAttributeNode(attr);

				attr = doc.createAttribute("head");
				attr.setValue(production.head.text);
				production_e.setAttributeNode(attr);

				int len = BNF.isNULLProduction(production) ? 0 : production.NodeSize();
				attr = doc.createAttribute("len");
				attr.setValue(Integer.toString(len));
				production_e.setAttributeNode(attr);

				StringBuilder sb = new StringBuilder();
				for (Symbol node : production.nodes) {
					if (sb.length() != 0) {
						sb.append("|");
					}
					sb.append(node.text);
				}
				attr = doc.createAttribute("nodes");
				attr.setValue(sb.toString());
				production_e.setAttributeNode(attr);
				if (!production.script.isEmpty()) {
					attr = doc.createAttribute("script");
					attr.setValue(production.script);
					production_e.setAttributeNode(attr);
				}

				root.appendChild(production_e);
			}

			for (State state : state_set.set()) {
				Element state_e = doc.createElement("state");
				Attr attr;

				attr = doc.createAttribute("id");
				attr.setValue(Integer.toString(state.id));
				state_e.setAttributeNode(attr);

				for (Entry<String, State> entry_g : state.goto_table.entrySet()) {
					Element item_e = doc.createElement("goto");

					attr = doc.createAttribute("on");
					attr.setValue(entry_g.getKey());
					item_e.setAttributeNode(attr);

					attr = doc.createAttribute("state");
					attr.setValue(Integer.toString(entry_g.getValue().id));
					item_e.setAttributeNode(attr);

					state_e.appendChild(item_e);
				}

				for (Entry<String, String> entry_p : state.parsing_table.entrySet()) {
					Element item_e = doc.createElement("action");

					attr = doc.createAttribute("on");
					attr.setValue(entry_p.getKey());
					item_e.setAttributeNode(attr);

					attr = doc.createAttribute("do");
					attr.setValue(entry_p.getValue());
					item_e.setAttributeNode(attr);

					state_e.appendChild(item_e);
				}

				root.appendChild(state_e);
			}
			return doc;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	Pair<State, Boolean> TryAddState(State state) {
		String hash_string = state.hashString();
		if (state_set.has(hash_string)) {
			return new Pair<State, Boolean>(state_set.get(hash_string), Boolean.FALSE);
		} else {
			state.assignID(state_set.size());
			state_set.add(state);
			return new Pair<State, Boolean>(state, Boolean.TRUE);
		}
	}

	ItemLR0 TryMakeItemLR0(Production production, int dot) {
		ItemLR0 new_item = new ItemLR0();
		new_item.production = production;
		new_item.dot = dot;
		
		if (item_lr0_set.has(new_item))
			return item_lr0_set.get(new_item);
		
		item_lr0_set.add(new_item);
		return new_item;
	}

	ItemLR0 TryMakeItemLR0IncDot(ItemLR0 item) {
		assert !item.EndWithDot();
		return TryMakeItemLR0(item.production, item.dot + 1);
	}

	ItemLR1 TryMakeItemLR1(ItemLR0 core, Symbol lookahead) {
		ItemLR1 new_item = new ItemLR1();
		new_item.core = core;
		new_item.lookahead = lookahead;
		
		if (item_lr1_set.has(new_item))
			return item_lr1_set.get(new_item);

		item_lr1_set.add(new_item);
		return new_item;
	}

	ItemLR1 TryMakeItemLR1(Production production, int dot, Symbol lookahead) {
		return TryMakeItemLR1(TryMakeItemLR0(production, dot), lookahead);
	}
}
class State implements IHash{
	public int id;
	public HashSet<ItemLR0> item_lr0_set_all;
	public HashSet<ItemLR0> item_lr0_set_kernel;
	public HashSet<ItemLR1> item_lr1_set_all;
	public HashSet<ItemLR1> item_lr1_set_kernel;

	LinkedHashMap<String, State> goto_table;
	LinkedHashMap<String, String> parsing_table;

	public State() {
		item_lr0_set_all = new HashSet<>();
		item_lr0_set_kernel = new HashSet<>();
		item_lr1_set_all = new HashSet<>();
		item_lr1_set_kernel = new HashSet<ItemLR1>();

		goto_table = new LinkedHashMap<String, State>();
		parsing_table = new LinkedHashMap<String, String>();
	}

	public void assignID(int id) {
		this.id = id;
	}
	@Override
	public String hashString() {
		return item_lr0_set_kernel.hashString();
	}
}