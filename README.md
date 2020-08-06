# Benchmark Results

| Runtime (s) | N=2,000,000 | xGeni | N=24,000,000 | xGeni |
| ---         | ---         | ---   | ---          | ---   |
| Pandas      | 587         | x73.4 | 1,132        | x29.0 |
| Dataset     | 220         | x27.5 | 726          | x18.6 |
| Geni        | 8           | x1.0  | 39           | x1    |

# Machine

## CPU

```
$ lscpu
Architecture:                    x86_64
CPU op-mode(s):                  32-bit, 64-bit
Byte Order:                      Little Endian
Address sizes:                   46 bits physical, 48 bits virtual
CPU(s):                          12
On-line CPU(s) list:             0-11
Thread(s) per core:              2
Core(s) per socket:              6
Socket(s):                       1
NUMA node(s):                    1
Vendor ID:                       GenuineIntel
CPU family:                      6
Model:                           63
Model name:                      Intel(R) Core(TM) i7-5930K CPU @ 3.50GHz
Stepping:                        2
CPU MHz:                         1617.393
CPU max MHz:                     3700.0000
CPU min MHz:                     1200.0000
BogoMIPS:                        6996.02
Virtualization:                  VT-x
L1d cache:                       192 KiB
L1i cache:                       192 KiB
L2 cache:                        1.5 MiB
L3 cache:                        15 MiB
NUMA node0 CPU(s):               0-11
...
```

## RAM

3 x 8GB of DDR4 RAM by Corsair:

```
$ sudo dmidecode -t memory | grep -i speed
        Size: 8192 MB
        Size: 8192 MB
        Size: 8192 MB
        ...
$ sudo dmidecode -t memory | grep -i speed
        Speed: 2133 MT/s
        Speed: 2133 MT/s
        Speed: 2133 MT/s
        ...
```

## SSD

Write:

```
$ sync; dd if=/dev/zero of=tempfile bs=1M count=1024; sync
1024+0 records in
1024+0 records out
1073741824 bytes (1.1 GB, 1.0 GiB) copied, 0.503625 s, 2.1 GB/s
```

Read:

```
$ dd if=tempfile of=/dev/null bs=1M count=1024
1024+0 records in
1024+0 records out
1073741824 bytes (1.1 GB, 1.0 GiB) copied, 0.132842 s, 8.1 GB/s
```
