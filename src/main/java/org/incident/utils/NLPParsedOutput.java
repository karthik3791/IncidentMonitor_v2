package org.incident.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import edu.stanford.nlp.trees.TypedDependency;

public class NLPParsedOutput {

	private Map<Integer, String> dateMap;
	private Map<Integer, String> organizationPreceededByPrepositionMap;
	private Map<Integer, String> organizationNotPreceededByPrepositionMap;
	private Map<Integer, String> locationMap;
	private Map<Integer, String> nounMap;
	private Map<Integer, String> verbMap;
	private Map<Integer, String> adjectiveMap;

	private Collection<TypedDependency> typedDependencyList;

	public NLPParsedOutput() {
		dateMap = new HashMap<Integer, String>();
		organizationPreceededByPrepositionMap = new HashMap<Integer, String>();
		organizationNotPreceededByPrepositionMap = new HashMap<Integer, String>();
		locationMap = new HashMap<Integer, String>();
		nounMap = new HashMap<Integer, String>();
		verbMap = new HashMap<Integer, String>();
		adjectiveMap = new HashMap<Integer, String>();

	}

	public Collection<TypedDependency> getTypedDependencyList() {
		return typedDependencyList;
	}

	public void setTypedDependencyList(Collection<TypedDependency> typedDependencyList) {
		this.typedDependencyList = typedDependencyList;
	}

	public void addDatePart(Integer wordpos, String datePart) {
		this.dateMap.put(wordpos, datePart);
	}

	public void addOrganizationPreceededByPreposition(Integer wordpos, String organizationPart) {
		this.organizationPreceededByPrepositionMap.put(wordpos, organizationPart);
	}

	public void addPlainOrganizationPart(Integer wordpos, String organizationPart) {
		this.organizationNotPreceededByPrepositionMap.put(wordpos, organizationPart);
	}

	public void resetPlainOrganizationPart() {
		this.organizationNotPreceededByPrepositionMap.clear();
	}
	public void addLocationMap(Integer wordpos, String locationPart) {
		this.locationMap.put(wordpos, locationPart);
	}

	public void addNounPart(Integer wordpos, String nounPart) {
		this.nounMap.put(wordpos, nounPart);
	}

	public void addVerbPart(Integer wordpos, String verbPart) {
		this.verbMap.put(wordpos, verbPart);
	}

	public void addAdjectivePart(Integer wordpos, String adjectivePart) {
		this.adjectiveMap.put(wordpos, adjectivePart);
	}

	public Map<Integer, String> getDateMap() {
		return dateMap;
	}

	public Map<Integer, String> getOrganizationPreceededByPrepositionMap() {
		return organizationPreceededByPrepositionMap;
	}

	public Map<Integer, String> getOrganizationNotPreceededByPrepositionMap() {
		return organizationNotPreceededByPrepositionMap;
	}

	public Map<Integer, String> getLocationMap() {
		return locationMap;
	}

	public Map<Integer, String> getNounMap() {
		return nounMap;
	}

	public Map<Integer, String> getVerbMap() {
		return verbMap;
	}

	public Map<Integer, String> getAdjectiveMap() {
		return adjectiveMap;
	}

}
