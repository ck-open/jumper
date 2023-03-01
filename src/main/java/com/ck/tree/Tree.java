package com.ck.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 树形结构数据
 *
 * @author cyk
 * @since 2020-01-01
 */
public class Tree<T> implements Cloneable {
    /**
     * 父节点Id
     */
    private Integer parentId;

    /**
     * 节点Id
     */
    private Integer id;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 节点类型Id
     */
    private Integer nodeTypeId;
    /**
     * 节点层级
     */
    private Integer nodeTier;

    /**
     * 节点类型名称
     */
    private String nodeTypeName;

    /**
     * 子节点列表
     */
    private List<Tree<T>> children;

    /**
     * 节点绑定的数据
     */
    private T nodeData;


    /**
     * 全参数构建
     *
     * @param parentId     父节点ID
     * @param id           节点ID
     * @param nodeName     节点名称
     * @param nodeTypeId   节点类型ID
     * @param nodeTypeName 节点类型名称
     * @param nodeData     节点挂载数据
     */
    public Tree(Integer parentId, Integer id, String nodeName, Integer nodeTypeId, String nodeTypeName, T nodeData) {
        this.parentId = parentId;
        this.id = id;
        this.nodeName = nodeName;
        this.nodeTypeId = nodeTypeId;
        this.nodeTypeName = nodeTypeName;
        this.nodeData = nodeData;
    }

    /**
     * 全参数构建
     *
     * @param parentId     父节点ID
     * @param id           节点ID
     * @param nodeName     节点名称
     * @param nodeTypeId   节点类型ID
     * @param nodeTypeName 节点类型名称
     */
    public Tree(Integer parentId, Integer id, String nodeName, Integer nodeTypeId, String nodeTypeName) {
        this.parentId = parentId;
        this.id = id;
        this.nodeName = nodeName;
        this.nodeTypeId = nodeTypeId;
        this.nodeTypeName = nodeTypeName;
    }

    /**
     * 全参数构建
     *
     * @param parentId 父节点ID
     * @param id       节点ID
     * @param nodeName 节点名称
     */
    public Tree(Integer parentId, Integer id, String nodeName) {
        this.parentId = parentId;
        this.id = id;
        this.nodeName = nodeName;
    }

    /**
     * 创建树结构
     *
     * @param trees 节点列表
     * @return
     */
    public void createTree(List<Tree<T>> trees) {
        if (trees != null && !trees.isEmpty()) {
            Map<Integer, List<Tree<T>>> treeMap = new HashMap<>();
            trees.forEach(item -> {
                List<Tree<T>> temp = treeMap.computeIfAbsent(item.getParentId(), k -> new ArrayList<>());
                temp.add(item);
            });
            this.nodeTier = this.nodeTier == null ? 1 : this.nodeTier;
            recursionTree(this, treeMap);
        }
    }

    /**
     * 递归封装树节点 到根上
     *
     * @param tree    根节点
     * @param treeMap 节点列表  Map<父节点ID,List<Tree>>
     */
    private void recursionTree(Tree<T> tree, Map<Integer, List<Tree<T>>> treeMap) {
        List<Tree<T>> children = treeMap.get(tree.getId());
        if (children != null && !children.isEmpty()) {
            tree.setChildren(children);  // 将子节点列表绑定到父节点  并递归每个节点
            treeMap.get(tree.getId()).forEach(item -> {
                item.setNodeTier(tree.getNodeTier() + 1); // 设置层级数
                recursionTree(item, treeMap);
            });
        }
    }

    /**
     * 获取末端节点Map  Key为节点ID
     *
     * @return
     */
    public Map<Integer, Tree<T>> lastNodesMap() {
        List<Tree<T>> trees = lastNodes(this, new ArrayList<>());
        Map<Integer, Tree<T>> treeMap = new HashMap<>();
        trees.forEach(item -> treeMap.put(item.getId(), item));
        return treeMap;
    }

    /**
     * 获取末端节点Map  Key为节点名称
     *
     * @return
     */
    public Map<String, Tree<T>> lastNodesMapOfNodeName() {
        List<Tree<T>> trees = lastNodes(this, new ArrayList<>());
        Map<String, Tree<T>> treeMap = new HashMap<>();
        trees.forEach(item -> treeMap.put(item.getNodeName(), item));
        return treeMap;
    }

    /**
     * 获取末端节点
     *
     * @return
     */
    public List<Tree<T>> lastNodes() {
        return lastNodes(this, new ArrayList<>());
    }

    /**
     * 递归获取末梢节点集合
     *
     * @param tree      父节点
     * @param lastNodes 获取列表的容器
     * @return
     */
    private List<Tree<T>> lastNodes(Tree<T> tree, List<Tree<T>> lastNodes) {
        if (tree.getChildren() == null || tree.getChildren().isEmpty()) {
            lastNodes.add(tree);
        } else {
            List<Tree<T>> trees = tree.getChildren();
            for (Tree<T> item : trees) {
                lastNodes(item, lastNodes);
            }
        }
        return lastNodes;
    }

    /**
     * 为树结构绑定数据
     *
     * @param dataMap<TreeID, T>
     */
    public void addNodeData(Map<Integer, T> dataMap) {
        for (Tree<T> item : this.lastNodes()) {
            item.setNodeData(dataMap.get(item.getId()));
        }
    }

    /**
     * 获取指定层级节点集合
     *
     * @param tier
     * @return
     */
    public List<Tree<T>> getNodeListByTier(int tier) {
        return this.getNodeListByTier(this, tier, new ArrayList<>());
    }

    /**
     * 递归获取指定层级节点集合
     *
     * @param tree  父节点
     * @param tier  指定的层级
     * @param nodes 获取列表的容器
     * @return
     */
    private List<Tree<T>> getNodeListByTier(Tree<T> tree, Integer tier, List<Tree<T>> nodes) {
        if (tree.getNodeTier() < tier && tree.getChildren() != null && !tree.getChildren().isEmpty()) {
            tree.getChildren().forEach(item -> getNodeListByTier(item, tier, nodes));
        } else if (tree.getNodeTier() != null && tree.getNodeTier().equals(tier)) {
            nodes.add(tree);
        }
        return nodes;
    }


    /**
     * 逐层条件查询
     *
     * @param tier        从指定层级开始
     * @param isQueryName 是否匹配名称 否则匹配ID
     * @param keys        逐层节点名称
     * @return
     */
    public Tree<T> getTreeNode(int tier, boolean isQueryName, Object... keys) {

        if (keys != null && keys.length > 0) {

            Tree<T> tempTree = this;
            for (int i = 0; i < keys.length; i++) {
                Object key = keys[i];
                if (key == null || "".equalsIgnoreCase(key.toString().trim()) || "null".equalsIgnoreCase(key.toString().trim())) {
                    break;
                }
                List<Tree<T>> treeList = tempTree.getNodeListByTier(i + tier);
                if (treeList != null) {
                    for (Tree<T> item : treeList)
                        if (isQueryName ? item.getNodeName().equalsIgnoreCase(key.toString()) : item.id.equals(key)) {
                            tempTree = item;
                            break;
                        }
                }
            }
            return tempTree;
        }
        return this;
    }

    /**
     * 逐层条件查询
     *
     * @param isQueryName 是否匹配名称 否则匹配ID
     * @param key         逐层节点名称
     * @return
     */
    public Tree<T> getTreeNode(boolean isQueryName, Object key) {
        if (isQueryName ? this.nodeName.equalsIgnoreCase(key.toString()) : this.id.equals(key))
            return this;
        if (this.children != null && !this.children.isEmpty())
            for (Tree<T> item : this.children) {
                if (item.getTreeNode(isQueryName, key) != null)
                    return item.getTreeNode(isQueryName, key);
            }
        return null;
    }

    /**
     * 获取指定名称的节点
     *
     * @param nodeName 指定的nodeName
     * @return
     */
    public Tree<T> getNodeByNodeName(String nodeName) {
        return getNodeByNodeName(this, nodeName);
    }

    /**
     * 递归获取指定名称的节点
     *
     * @param tree     结构树
     * @param nodeName 指定的nodeName
     * @return
     */
    private Tree<T> getNodeByNodeName(Tree<T> tree, String nodeName) {
        Tree<T> result = null;
        if (tree.getNodeName() != null && tree.equals(nodeName)) {
            result = tree;
        } else {
            if (tree.getChildren() != null && !tree.getChildren().isEmpty()) {
                for (Tree<T> item : tree.getChildren()) {
                    if (item.getNodeName().equals(nodeName)) {
                        return item;
                    }
                }
                for (Tree<T> item : tree.getChildren()) {
                    result = getNodeByNodeName(item, nodeName);
                }
            }
        }
        return result;
    }

//    /**
//     * 树在最大层级
//     */
//    private Integer maxTier = 1;
//
//    /**
//     * 获取该树结构最大层级
//     *
//     * @return
//     */
//    public Integer getMaxTier() {
//        return getMaxTier(1);
//    }
//
//    /**
//     * 获取该树结构最大层级
//     *
//     * @return
//     */
//    public Integer getMaxTier(int tier) {
//        Integer result = 1;
//        if (this.children != null) {
//            ++tier;
//            for (Tree<T> item : this.children)
//                result = item.getMaxTier(tier);
//            if (this.id.equals(0)){
//                System.out.println();
//            }
//        }
//        if (tier > result){
//            result = tier;
//            tier = 1;
//        }
//        return result;
//    }

    /**
     * 将树结构递归成表格
     *
     * @return
     */
    public List<List<Tree<T>>> toTable() {
        return toTable(this, new ArrayList<>(), new ArrayList<>());
    }

    /**
     * 将树结构递归成表格
     *
     * @param tree  结构树
     * @param table 表
     * @param row   行
     * @return
     */
    private List<List<Tree<T>>> toTable(Tree<T> tree, List<List<Tree<T>>> table, List<Tree<T>> row) {
        if (tree != null && tree.getChildren() != null && !tree.getChildren().isEmpty()) {
            row.add(tree);  // 有子集  则将本节点添加到行
            for (Tree<T> item : tree.getChildren()) {
                toTable(item, table, new ArrayList<>(row));
            }
        } else {
            row.add(tree);
            table.add(row);
        }
        return table;
    }

    /**
     * 深度复制整棵树
     *
     * @return
     */
    public Tree<T> copy() {
        return copy(this);
    }

    /**
     * 深度复制整棵树
     *
     * @param tTree
     * @return
     */
    private Tree<T> copy(Tree<T> tTree) {
        Tree<T> newTree = tTree.clone();
        if (newTree.getChildren() != null && !newTree.getChildren().isEmpty()) {
            newTree.setChildren(new ArrayList<>());
            for (Tree<T> item : tTree.getChildren()) {
                newTree.getChildren().add(copy(item));
            }
        }
        return newTree;
    }

    @Override
    protected Tree<T> clone() {
        try {
            return (Tree<T>) super.clone();
        } catch (Exception e) {
            System.out.println("==> Tree 对象复制失败！  " + e.getMessage());
        }
        return null;
    }


    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public Integer getNodeTypeId() {
        return nodeTypeId;
    }

    public void setNodeTypeId(Integer nodeTypeId) {
        this.nodeTypeId = nodeTypeId;
    }

    public String getNodeTypeName() {
        return nodeTypeName;
    }

    public void setNodeTypeName(String nodeTypeName) {
        this.nodeTypeName = nodeTypeName;
    }

    public T getNodeData() {
        return nodeData;
    }

    public void setNodeData(T nodeData) {
        this.nodeData = nodeData;
    }

    public List<Tree<T>> getChildren() {
        return children;
    }

    public void setChildren(List<Tree<T>> children) {
        this.children = children;
    }

    public Integer getNodeTier() {
        return nodeTier;
    }

    public void setNodeTier(Integer nodeTier) {
        this.nodeTier = nodeTier;
    }

}
