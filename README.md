# Benchmark Results

| Language | Runtime (s)                          | N=2,000,000 | xGeni | N=24,000,000 | xGeni |
| --       | ---                                  | ---         | ---   | ---          | ---   |
| Python   | Pandas                               | 587         | x73.4 | 1,132        | x29.0 |
| R        | dplyr                                | 461         | x57.6 | 992          | x25.4 |
| Julia    | DataFrames (with Parquet)            | 87          | x10.9 | 868          | x22.3 |
| Clojure  | tablecloth                           | 48          | x6.0  | 151          | x3.9  |
| R        | data.table                           | 28          | x3.5  | 143          | x3.7  |
| Clojure  | tech.ml.dataset (optimised)          | 18          | x2.3  | 133          | x3.4  |
| Julia    | DataFrames (with Feather)            | 16          | x2.0  | 41           | x1.1  |
| Clojure  | tech.ml.dataset (optimised by Chris) | 9           | x1.1  | 36           | x0.9  |
| Clojure  | Geni                                 | 8           | x1.0  | 39           | x1.0  |

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

3 x 8GB of Corsair's DDR4 RAM:

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

```
$ sudo hwinfo --disk
97: PCI 00.0: 10600 Disk
  [Created at block.245]
  ...
  Hardware Class: disk
  Model: "Samsung Electronics NVMe SSD Controller SM981/PM981"
  Vendor: pci 0x144d "Samsung Electronics Co Ltd"
  Device: pci 0xa808 "NVMe SSD Controller SM981/PM981"
  SubVendor: pci 0x144d "Samsung Electronics Co Ltd"
  SubDevice: pci 0xa801
  Driver: "nvme"
  Driver Modules: "nvme"
  Device File: /dev/nvme0n1
  Device Files: /dev/nvme0n1, /dev/disk/by-id/nvme-Samsung_SSD_970_EVO_Plus_500GB_S4EVNG0M801407P, /dev/disk/by-id/nvme-eui.002538589150082c, /dev/disk/by-path/pci-0000:05:00.0-nvme-1
  Device Number: block 259:0
  BIOS id: 0x80
  Geometry (Logical): CHS 476940/64/32
  Size: 976773168 sectors a 512 bytes
  Capacity: 465 GB (500107862016 bytes)
  Config Status: cfg=new, avail=yes, need=no, active=unknown
  Attached to: #67 (Non-Volatile memory controller)
```

Write speed:

```
$ sync; dd if=/dev/zero of=tempfile bs=1M count=1024; sync
1024+0 records in
1024+0 records out
1073741824 bytes (1.1 GB, 1.0 GiB) copied, 0.503625 s, 2.1 GB/s
```

Read speed:

```
$ dd if=tempfile of=/dev/null bs=1M count=1024
1024+0 records in
1024+0 records out
1073741824 bytes (1.1 GB, 1.0 GiB) copied, 0.132842 s, 8.1 GB/s
```
