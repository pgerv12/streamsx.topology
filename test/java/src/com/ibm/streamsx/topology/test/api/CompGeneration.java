/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015  
 */
package com.ibm.streamsx.topology.test.api;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.ibm.streamsx.topology.TStream;
import com.ibm.streamsx.topology.TStream.Routing;
import com.ibm.streamsx.topology.Topology;
import com.ibm.streamsx.topology.test.TestTopology;
import com.ibm.streamsx.topology.tester.Condition;
import com.ibm.streamsx.topology.tester.Tester;

public class CompGeneration extends TestTopology {

    @Test
    public void parallelSource() throws Exception {
        assumeTrue(SC_OK);
        
        Topology topo = new Topology();
        
        TStream<String> nums = topo.strings("1", "2", "3", "4", "5");
        nums.setParallel(() -> 4);
        
        nums = nums.endParallel();
        
        Tester tester = topo.getTester();
        Condition<List<String>> validCount = tester.stringContentsUnordered(nums,
                "1", "2", "3", "4", "5",
                "1", "2", "3", "4", "5",
                "1", "2", "3", "4", "5",
                "1", "2", "3", "4", "5");
        

        complete(tester, allConditions(validCount), 10, TimeUnit.SECONDS);

        assertTrue(validCount.valid());
    }
    
    @Test
    public void nestedUDPWithBroadcast() throws Exception {
        assumeTrue(SC_OK);
        
        Topology topo = new Topology();
        
        TStream<String> nums = topo.strings("1", "2");
        nums.setParallel(() -> 2);
        
        
        nums = nums.parallel(() -> 4, Routing.BROADCAST);
        nums = nums.filter(tup -> true);
        nums = nums.endParallel().endParallel();
        
        Tester tester = topo.getTester();
        Condition<List<String>> validCount = tester.stringContentsUnordered(nums,
                "1", "1", "1", "1", "1", "1", "1", "1",
                "2", "2", "2", "2", "2", "2", "2", "2");
        

        complete(tester, allConditions(validCount), 10, TimeUnit.SECONDS);

        assertTrue(validCount.valid());
    }
    
    @Test
    public void multipleInputPorts() throws Exception {
        assumeTrue(SC_OK);
        
        Topology topo = new Topology();
        
        TStream<String> nums = topo.strings("1");
        TStream<String> nums2 = topo.strings("2");
        TStream<String> nums3 = topo.strings("3");
        
        nums = nums.parallel(3);
        nums2 = nums2.parallel(3);
        nums3 = nums3.parallel(3);
        
        Set<TStream<String>> streams = new HashSet<>();
        streams.add(nums2);
        streams.add(nums3);
        nums = nums.union(streams);
        
        nums = nums.filter(tup -> true);
        nums = nums.endParallel();
        
        
        Tester tester = topo.getTester();
        Condition<List<String>> validCount = tester.stringContentsUnordered(nums,
                "1", "2", "3");
        

        complete(tester, allConditions(validCount), 10, TimeUnit.SECONDS);

        assertTrue(validCount.valid());
    }
    
    @Test
    public void multipleInputPortsDifferentRoutingSchemes() throws Exception {
        assumeTrue(SC_OK);
        
        Topology topo = new Topology();
        
        TStream<String> nums = topo.strings("1");
        TStream<String> nums2 = topo.strings("2");
        TStream<String> nums3 = topo.strings("3");
        
        nums = nums.parallel(() -> 3, tup -> 1);
        nums2 = nums2.parallel(() -> 3, Routing.BROADCAST);
        nums3 = nums3.parallel(3);
        
        Set<TStream<String>> streams = new HashSet<>();
        streams.add(nums2);
        streams.add(nums3);
        nums = nums.union(streams);
        
        nums = nums.filter(tup -> true);
        nums = nums.endParallel();
        
        
        Tester tester = topo.getTester();
        Condition<List<String>> validCount = tester.stringContentsUnordered(nums,
                "1", "2", "2", "2", "3");
        

        complete(tester, allConditions(validCount), 10, TimeUnit.SECONDS);

        assertTrue(validCount.valid());
    }
    
    
}