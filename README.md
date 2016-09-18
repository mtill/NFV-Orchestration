# Simulation Toolkit for NFV Orchestration

This is a framework that enables researchers to simulate deployment/orchestration algorithms in the context of NFV (network function virtualization).
It is quite straight forward to implement new strategies for the deployment of VNFs (virtual network functions) and to compare them to existing approaches (CoordVNF and SVNF).

This framework comes with an implementation of two deployment algorithms that were presented at two conferences, namely the CoordVNF and the SVNF algorithm.


Author: Michael Till Beck <michael.beck@ifi.lmu.de>

http://www.michaeltillbeck.de



## Implementation of the CoordVNF algorithm

Title: Coordinated Allocation of Service Function Chains

Published in: Proceedings of the IEEE Global Communications Conference (GLOBECOM), 2015

Authors: Michael Till Beck and Juan-Felipe Botero

URL: http://ieeexplore.ieee.org/document/7417401/

Abstract:
> "Network Functions Virtualization (NFV) is an emerging initiative to overcome increasing operational and capital costs faced by network operators due to the need to physically locate network functions in specific hardware appliances. In NFV, standard IT virtualization evolves to consolidate network functions onto high volume servers, switches and storage that can be located anywhere in the network. Services are built by chaining a set of Virtual Network Functions (VNFs) deployed on commodity hardware. The implementation of NFV leads to the challenge: How several network services (VNF chains) are optimally orchestrated and allocated on the substrate network infrastructure? In this paper, we address this problem and propose CoordVNF, a heuristic method to coordinate the composition of VNF chains and their embedding into the substrate network. CoordVNF aims to minimize bandwidth utilization while computing results within reasonable runtime."



## Implementation of the SVNF algorithm

Title: Resilient Allocation of Service Function Chains

Authors: Michael Till Beck and Juan-Felipe Botero and Kai Samelin

Published in: Proceedings of the IEEE Conference on Network Function Virtualization and Software Defined Networks (NFV-SDN), 2016 (to appear)

Abstract:
> "Network Functions Virtualization (NFV) is an emerging initiative where standard IT virtualization evolves to consolidate network functions onto high volume servers, switches and storage that can be located anywhere in the network. One of the main challenges to implement NFV is the problem of allocating the Virtual Network Functions (VNFs) on top of the physical network infrastructure. Up to now, existing approaches dealing with this problem have not considered the possibility of failures in this infrastructure.
> This paper discusses several measures on how backup resources can be integrated into the embedding of virtual network functions in order to protect network services from failures; furthermore, a resource allocation algorithm is proposed that considers resilience constraints. The algorithm exploits the benefits of sharing backup network resources in order to reduce the resource cost spent for providing resilient allocations."


# Requirements

Alevin 2.2 (http://alevin.sf.net/)


# If you like this code, please read and cite

    @inproceedings{CoordVNF,
        author={Michael Till Beck and Juan-Felipe Botero},
        booktitle={2015 IEEE Global Communications Conference (GLOBECOM)},
        title={Coordinated Allocation of Service Function Chains},
        year={2015},
        pages={1-6},
        keywords={Internet;CoordVNF;Internet;VNF chains;bandwidth utilization;coordinated allocation;heuristic method;network functions virtualization;service function chains;standard IT virtualization;virtual network functions;Bandwidth;Hardware;Resource management;Runtime;Servers;Substrates;Virtualization},
        doi={10.1109/GLOCOM.2015.7417401},
        month={Dec}
    }

