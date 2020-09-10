import time

# Version: 0.25.2
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
    transactions = pd.read_parquet('/data/performance-benchmark-data')
    # transactions = pd.read_parquet('/data/performance-benchmark-data/part-00000-0cf99dad-6d07-4025-a5e9-f425bb9532b9-c000.snappy.parquet')
    transactions['sales'] = transactions['price'] * transactions['quantity']
    matrix = (
        transactions
        .groupby('member-id')
        .apply(lambda grouped: pd.Series({
            'total-spend': grouped['sales'].sum(),
            'avg-basket-size': grouped['sales'].mean(),
            'avg-price': grouped['price'].mean(),
            'n-transactions': len(grouped),
            'n-visits': len(grouped['date'].unique()),
            'n-brands': len(grouped['brand-id'].unique()),
            'n-styles': len(grouped['style-id'].unique()),
        })))
    matrix.to_parquet('pandas-matrix.parquet')

if __name__ == '__main__':
    write_matrix()

    # 1 Part
    # Starting write_matrix...
    # Finished write_matrix in 587.2644765377045s

    # 12 Parts
    # Starting write_matrix...
    # Finished write_matrix in 1131.992933511734s
