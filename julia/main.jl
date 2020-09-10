using Glob, ParquetFiles, DataFrames, Statistics, Queryverse

function nunique(x)
    size(unique(x), 1)
end

function run()
    ;files = glob("*.snappy.parquet", "/data/julia/")
    files = glob("*.feather", "/data/julia/")
    dfs = DataFrame.(load.(files)) 
    df = reduce(vcat, dfs)
    df.sales = df.price .* df.quantity
    gdf = groupby(df, :member_id)
    summary = combine(gdf, :sales => sum => :total_spend,
                           :sales => mean => :avg_basket_size,
                           :price => mean => :avg_price,
                           nrow => :n_transactions,
                           :date => nunique => :n_visits,
                           :brand_id => nunique => :n_brands,
                           :style_id => nunique => :n_styles)
    summary |> save("julia.feather")
end

@time run()

; Parquet
; 1 Part: 86.532695 seconds (502.42 M allocations: 24.133 GiB, 6.02% gc time)
; 12 Parts: run: 868.278368 seconds (5.25 G allocations: 252.436 GiB, 19.38% gc time)

; Feather
; 1 Part: 16.211124 seconds (55.16 M allocations: 3.250 GiB, 6.64% gc time)
; 12 Parts: 41.322144 seconds (109.18 M allocations: 11.552 GiB, 25.66% gc time)

; function save_feather(file)
;     df = DataFrame(load(file))
;     new_file = replace(file, "snappy.parquet" => "feather") 
;     df |> save(new_file)
; end
; 
; files = glob("*.snappy.parquet", "/data/julia/")
; for file in files
;     save_feather(file)
; end

df = DataFrame(load("julia.feather"))
size(df, 1)
size(df, 2)
