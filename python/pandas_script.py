import time

# Version: 1.0.5
import pandas as pd

def timer(fn):
    def wrapped(*args, **kwargs):
        name = fn.__name__
        print(f'Starting {name}...')
        start = time.time()
        out = fn(*args, **kwargs)
        stop = time.time()
        print(f'Finished {name} in {stop - start}s')
        return out
    return wrapped

@timer
def write_matrix():
    # transactions = pd.read_parquet('/data/performance-benchmark-data')
    transactions = pd.read_parquet('/data/performance-benchmark-data/part-00000-0cf99dad-6d07-4025-a5e9-f425bb9532b9-c000.snappy.parquet')
    transactions['sales'] = transactions['price'] * transactions['quantity']
    matrix = (
        transactions
        .groupby('member-id')
        .agg({
            'sales': ['sum', 'mean'],
            'price': ['mean'],
            'trx-id': ['count'],
            'date': ['nunique'],
            'brand-id': ['nunique'],
            'style-id': ['nunique'],
        })
        .reset_index())
    matrix.columns = ['-'.join(col).strip() for col in matrix.columns.values]
    matrix.to_parquet('pandas-matrix.parquet')

if __name__ == '__main__':
    write_matrix()

    # 1 Part
    # Starting write_matrix...
    # Finished write_matrix in 587.2644765377045s
    # Finished write_matrix in 3.1726512908935547s

    # 12 Parts
    # Starting write_matrix...
    # Finished write_matrix in 1131.992933511734s
    # Finished write_matrix in 41.91314244270325s
