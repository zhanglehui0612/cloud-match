package com.cloud.match.cluster;

import java.util.List;

public interface Cluster {

    void start() throws Exception;

    void close();

    void join() throws Exception;

    List<Node> members();

    boolean isLeader();

    Node getNode();
}
