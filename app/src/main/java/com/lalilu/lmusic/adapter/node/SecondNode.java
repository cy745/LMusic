package com.lalilu.lmusic.adapter.node;

import com.chad.library.adapter.base.entity.node.BaseExpandNode;
import com.chad.library.adapter.base.entity.node.BaseNode;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SecondNode<T> extends BaseExpandNode {

    private List<BaseNode> childNode;
    public T data;

    public SecondNode(List<BaseNode> childNode, T data) {
        this.childNode = childNode;
        this.data = data;

        setExpanded(false);
    }

    @Nullable
    @Override
    public List<BaseNode> getChildNode() {
        return childNode;
    }
}
