package fr.lirmm.graphik.graal.rulesetanalyser;

import java.util.Map;
import java.util.TreeMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Deque;

import fr.lirmm.graphik.util.graph.scc.StronglyConnectedComponentsGraph;
import fr.lirmm.graphik.graal.core.Rule;
import fr.lirmm.graphik.graal.rulesetanalyser.util.AnalyserRuleSet;
import fr.lirmm.graphik.graal.rulesetanalyser.property.RuleSetProperty;
import fr.lirmm.graphik.graal.rulesetanalyser.property.BTSProperty;
import fr.lirmm.graphik.graal.rulesetanalyser.property.FESProperty;
import fr.lirmm.graphik.graal.rulesetanalyser.property.FUSProperty;

public class Analyser {

	public static final int COMBINE_NONE = 0;
	public static final int COMBINE_FES  = (1 << 0);
	public static final int COMBINE_FUS  = (1 << 1);
	public static final int COMBINE_BTS  = (1 << 2);

	private AnalyserRuleSet             ruleSet;
	private RuleSetPropertyHierarchy    hierarchy;
	private List<Map<String,Integer>>   sccProperties;
	private Map<String,Integer>         ruleSetProperties;
	public Analyser() { }

	public void setRuleSet(AnalyserRuleSet rules) {
		this.ruleSet = rules;
	}
	public void setRuleSet(Iterable<Rule> rules) {
		this.ruleSet = new AnalyserRuleSet(rules);
	}

	public void setProperties(RuleSetPropertyHierarchy h) {
		this.hierarchy = h;
	}
	public void setProperties(Iterable<RuleSetProperty> pties) {
		this.hierarchy = new RuleSetPropertyHierarchy(pties);
	}

	/**
	 * @return true only if some property ensures the rule set is decidable
	 */
	public boolean isDecidable() {
		return false;
	}

	public Map<String,Integer> ruleSetProperties() {
		if (this.ruleSetProperties == null)
			this.ruleSetProperties = computeProperties(this.ruleSet);
		return this.ruleSetProperties;
	}

	public List<Map<String,Integer>> sccProperties() {
		if (this.sccProperties == null) {
			this.sccProperties = new LinkedList<Map<String,Integer>>();
			for (AnalyserRuleSet subAnalyser : this.ruleSet.getSCC()) {
				this.sccProperties.add(computeProperties(subAnalyser));
			}
		}
		return this.sccProperties;
	}

	public int[] combineFES() {
		final int n = this.ruleSet.getSCC().size();
		final StronglyConnectedComponentsGraph<Rule> scc = this.ruleSet.getStronglyConnectedComponentsGraph();
		final int[] layers = scc.computeLayers(scc.getSources(), true);
		boolean[] mark = new boolean[n];
		int[] result = prepareCombine();
		int i;

		for (i = 0 ; i < n ; ++i)
			mark[i] = false;

		Deque<Integer> waiting = new LinkedList<Integer>();
		for (Integer s : scc.getSources()) {
			waiting.addLast(s);
			mark[s.intValue()] = true;
		}

		int s;
		while (!waiting.isEmpty()) {
			s = waiting.pollFirst();
			if (result[s] == 0) return null;
			if ((result[s] & COMBINE_FES) != 0) result[s] = COMBINE_FES;
			else if ((result[s] & COMBINE_FUS) != 0) result[s] = COMBINE_FUS;
			for (int t : scc.outgoingEdgesOf(s)) {
				int succ = scc.getEdgeTarget(t);
				if ((result[s] & COMBINE_FES) == 0) {
					result[succ] ^= COMBINE_FES;
					result[succ] ^= COMBINE_BTS;
				}
				if (!mark[succ] && (layers[s] + 1 == layers[succ])) {
					mark[succ] = true;
					waiting.addLast(succ);
				}
			}
		}

		return result;
	}

	public int[] combineFUS() {
		final int n = this.ruleSet.getSCC().size();
		final StronglyConnectedComponentsGraph<Rule> scc = this.ruleSet.getStronglyConnectedComponentsGraph();
		final int[] layers = scc.computeLayers(scc.getSinks(), false);
		boolean[] mark = new boolean[n];
		int[] result = prepareCombine();
		int i;

		for (i = 0 ; i < n ; ++i)
			mark[i] = false;

		Deque<Integer> waiting = new LinkedList<Integer>();
		for (Integer s : scc.getSinks()) {
			waiting.addLast(s);
			mark[s.intValue()] = true;
		}

		int s;
		while (!waiting.isEmpty()) {
			s = waiting.pollFirst();
			if (result[s] == 0) return null;
			if ((result[s] & COMBINE_FUS) != 0) result[s] = COMBINE_FUS;
			else if ((result[s] & COMBINE_FES) != 0) result[s] = COMBINE_FES;
			for (int t : scc.incomingEdgesOf(s)) {
				int succ = scc.getEdgeSource(t);
				if ((result[s] & COMBINE_FUS) == 0) {
					result[succ] ^= COMBINE_FUS;
					result[succ] ^= COMBINE_BTS;
				}
				if (!mark[succ] && (layers[s] + 1 == layers[succ])) {
					mark[succ] = true;
					waiting.addLast(succ);
				}
			}
		}

		return result;
	}



	protected int[] prepareCombine() {
		final int n = this.ruleSet.getSCC().size();
		int[] result = new int[n];
		int i = 0;
		List<Map<String,Integer>> sccPties = this.sccProperties();
		for (Map<String,Integer> scc : sccPties) {
			result[i] = 0;
			if (scc.get(FESProperty.instance().getLabel()) != null
			 && scc.get(FESProperty.instance().getLabel()) != 0)
				result[i] |= COMBINE_FES;
			if (scc.get(FUSProperty.instance().getLabel()) != null
			 && scc.get(FUSProperty.instance().getLabel()) != 0)
				result[i] |= COMBINE_FUS;
			if (scc.get(BTSProperty.instance().getLabel()) != null
			 && scc.get(BTSProperty.instance().getLabel()) != 0)
				result[i] |= COMBINE_BTS;
			++i;
		}
		return result;
	}

	protected Map<String,Integer> computeProperties(AnalyserRuleSet set) {
		Map<String, Integer> result = new TreeMap<String, Integer>();
		Iterable<RuleSetProperty> pties = this.hierarchy.getOrderedProperties();
		int res;
		for (RuleSetProperty p : pties) {
			if (result.get(p.getLabel()) == null) {
				res = p.check(set);
				result.put(p.getLabel(), new Integer(res));
				if (res > 0) {
					for (RuleSetProperty p2 : this.hierarchy.getGeneralisationsOf(p))
						result.put(p2.getLabel(), new Integer(res));
				}
			}
		}
		return result;
	}

};

