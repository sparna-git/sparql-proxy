# SPARQL Proxy

A SPARQL proxy server, deployed at https://proxy.sparnatural.eu.
This proxy avoids 2 security issues when querying a SPARQL service from a webpage (e.g. from [Sparnatural](https://sparnatural.eu)):
1. SPARQL servers that are not CORS-enabled
2. SPARQL services that do no run on https, while the client does
