package org.flymine.objectstore.query;

/*
 * Copyright (C) 2002-2003 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.flymine.objectstore.query.fql.FqlQuery;

/**
 * This class provides an implementation-independent abstract representation of a query
 *
 * @author Mark Woodbridge
 * @author Richard Smith
 * @author Matthew Wakeling
 */
public class Query implements FromElement
{
    private boolean distinct = true;
    private Constraint constraint = null;
    private Set queryClasses = new LinkedHashSet(); // @element-type FromElement
    private List select = new ArrayList(); // @element-type QueryNode
    private List orderBy = new ArrayList(); // @element-type QueryNode
    private Set groupBy = new LinkedHashSet(); // @element-type QueryNode
    private Map aliases = new HashMap();
    private Map reverseAliases = new HashMap();

    private int aliasNo = 1;

    /**
     * Empty constructor.
     */
    public Query() {
    }

    /**
     * Adds a FromElement to the FROM clause of this Query
     *
     * @param cls the FromElement to be added
     * @return the updated Query
     */
    public Query addFrom(FromElement cls) {
        if (cls == null) {
            throw new NullPointerException("cls must not be null");
        }
        queryClasses.add(cls);
        alias(cls, null);
        return this;
    }

    /**
     * Adds a FromElement to the FROM clause of this Query
     *
     * @param cls the FromElement to be added
     * @param alias the alias for this FromElement
     * @return the updated Query
     */
    public Query addFrom(FromElement cls, String alias) {
        if (cls == null) {
            throw new NullPointerException("cls must not be null");
        }
        queryClasses.add(cls);
        alias(cls, alias);
        return this;
    }

    /**
     * Remove a FromElement from the FROM clause
     *
     * @param cls the FromElement to remove
     * @return the updated Query
     */
    public Query deleteFrom(FromElement cls) {
        queryClasses.remove(cls);
        return this;
    }

    /**
     * Returns all FromElements in the FROM clause
     *
     * @return list of FromElements
     */
    public Set getFrom() {
        return Collections.unmodifiableSet(queryClasses);
    }

    /**
       * Constrain this Query using either a single constraint or a set of constraints
       *
       * @param constraint the constraint or constraint set
       * @return the updated query
       */
    public Query setConstraint(Constraint constraint) {
        this.constraint = constraint;
        return this;
    }

    /**
       * Get the current constraint on this Query
       *
       * @return the constraint
       */
    public Constraint getConstraint() {
        return constraint;
    }

    /**
     * Add a QueryNode to the GROUP BY clause of this Query
     *
     * @param node the node to add
     * @return the updated Query
     */
    public Query addToGroupBy(QueryNode node) {
        groupBy.add(node);
        return this;
    }

    /**
     * Remove a QueryNode from the GROUP BY clause
     *
     * @param node the node to remove
     * @return the updated Query
     */
    public Query deleteFromGroupBy(QueryNode node) {
        groupBy.remove(node);
        return this;
    }

    /**
     * Gets the GROUP BY clause of this Query
     *
     * @return the set of GROUP BY nodes
     */
    public Set getGroupBy() {
        return Collections.unmodifiableSet(groupBy);
    }

    /**
     * Add a QueryNode to the ORDER BY clause of this Query
     *
     * @param node the node to add
     * @return the updated Query
     */
    public Query addToOrderBy(QueryNode node) {
        orderBy.add(node);
        return this;
    }

    /**
     * Remove a QueryNode from the ORDER BY clause
     *
     * @param node the node to remove
     * @return the updated Query
     */
    public Query deleteFromOrderBy(QueryNode node) {
        orderBy.remove(node);
        return this;
    }

    /**
     * Gets the ORDER BY clause of this Query
     *
     * @return the List of ORDER BY nodes
     */
    public List getOrderBy() {
        return Collections.unmodifiableList(orderBy);
    }

    /**
     * Add a QueryNode to the SELECT clause of this Query
     *
     * @param node the QueryNode to add
     * @return the updated Query
     */
    public Query addToSelect(QueryNode node) {
        select.add(node);
        alias(node, null);
        return this;
    }

    /**
     * Add a QueryNode to the SELECT clause of this Query
     *
     * @param node the QueryNode to add
     * @param alias the alias for this FromElement
     * @return the updated Query
     */
    public Query addToSelect(QueryNode node, String alias) {
        select.add(node);
        alias(node, alias);
        return this;
    }

    /**
     * Remove a QueryNode from the SELECT clause
     *
     * @param node the node to remove
     * @return the updated Query
     */
    public Query deleteFromSelect(QueryNode node) {
        select.remove(node);
        String alias = (String) aliases.remove(node);
        if (alias != null) {
            reverseAliases.remove(alias);
        }
        return this;
    }

    /**
     * Gets the SELECT list
     *
     * @return the (unmodifiable) list
     */
    public List getSelect() {
        return Collections.unmodifiableList(select);
    }

    /**
     * Get the value of the distinct property
     *
     * @return the value of distinct
     */
    public boolean isDistinct() {
        return distinct;
    }

    /**
     * Set the value of the distinct property, which determines whether duplicates are
     * permitted in the results returned by this Query
     *
     * @param distinct the value of distinct
     */
    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    /**
     * Returns the map of SELECTed QueryNodes to String aliases
     *
     * @return the map
     */
    public Map getAliases() {
        return Collections.unmodifiableMap(aliases);
    }

    /**
     * Returns the map of String aliases to SELECTed QueryNodes
     *
     * @return the map
     */
    public Map getReverseAliases() {
        return Collections.unmodifiableMap(reverseAliases);
    }

    /**
     * Returns a string representation of this Query object
     *
     * @return a String representation
     */
    public String toString() {
        FqlQuery fq = new FqlQuery(this);
        return fq.toString();
    }

    /**
     * Set an alias for an element in the Query
     *
     * @param obj the element to alias
     * @param alias the alias to give
     */
    protected void alias(Object obj, String alias) {

        if ((alias != null) && reverseAliases.containsKey(alias)
            && (!obj.equals(reverseAliases.get(alias)))) {
            throw new IllegalArgumentException("Alias " + alias + " is already in use");
        }

        if ((alias != null) && aliases.containsKey(obj)
            && (!alias.equals(aliases.get(obj)))) {
            throw new IllegalArgumentException("Cannot re-alias the same element");
        }

        if (!aliases.containsKey(obj)) {
            if (alias == null) {
                alias = "a" + (aliasNo++) + "_";
            }
            aliases.put(obj, alias);
            reverseAliases.put(alias, obj);
        }
    }

    /**
     * Overrides Object.equals()
     *
     * @param obj and Object to compare to
     * @return true if object is equivalent
     */
    public boolean equals(Object obj) {
        if (obj instanceof Query) {
            Query q = (Query) obj;
            return (distinct == q.distinct) && select.equals(q.select)
                && queryClasses.equals(q.queryClasses)
                && (constraint != null) ? (constraint.equals(q.constraint)) : (q.constraint == null)
                && groupBy.equals(q.groupBy) && orderBy.equals(q.orderBy)
                && aliases.equals(q.aliases);
        }
        return false;
    }

    /**
     * Overrides Object.hashCode().
     *
     * @return an arbitrary integer created from the contents of the Query
     */
    public int hashCode() {
        return (distinct ? 29 : 0) + (5 * select.hashCode())
            + (7 * queryClasses.hashCode())
            + ((constraint != null) ? constraint.hashCode() : 31)
            + (13 * groupBy.hashCode()) + (15 * orderBy.hashCode())
            + (17 * aliases.hashCode());
    }


}
