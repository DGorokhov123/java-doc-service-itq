# EXPLAIN.md — Анализ производительности запросов

## EXPLAIN ANALYZE

```sql
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT d.* 
FROM documents d
WHERE d.status = 'SUBMITTED'
  AND d.author = 'Иванов И.И.'
  AND d.created_at >= '2026-01-01 00:00:00'
  AND d.created_at <= '2026-12-31 23:59:59'
ORDER BY d.created_at DESC
LIMIT 20 OFFSET 0;
```

### Пример плана выполнения

```
Limit  (cost=4.31..4.32 rows=1 width=96) (actual time=0.099..0.099 rows=0 loops=1)
  Buffers: shared hit=7 dirtied=1
  ->  Sort  (cost=4.31..4.32 rows=1 width=96) (actual time=0.098..0.098 rows=0 loops=1)
        Sort Key: created_at DESC
        Sort Method: quicksort  Memory: 25kB
        Buffers: shared hit=7 dirtied=1
        ->  Index Scan using idx_documents_status on documents d  (cost=0.28..4.30 rows=1 width=96) (actual time=0.077..0.077 rows=0 loops=1)
              Index Cond: ((status)::text = 'SUBMITTED'::text)
              Filter: ((created_at >= '2026-01-01 00:00:00'::timestamp without time zone) AND (created_at <= '2026-12-31 23:59:59'::timestamp without time zone) AND ((author)::text = 'Иванов И.И.'::text))
              Buffers: shared hit=4 dirtied=1
Planning:
  Buffers: shared hit=209 dirtied=1
Planning Time: 3.308 ms
Execution Time: 0.147 ms
```

## Индексы таблицы документов

```sql
-- Индекс по статусу, полезен для фильтрации по статусу
CREATE INDEX idx_documents_status ON documents(status);

-- Индекс по автору, ускоряет поиск по автору
CREATE INDEX idx_documents_author ON documents(author);

-- Индекс по дате создания, критически важен для сортировки и фильтрации по периоду
CREATE INDEX idx_documents_created_at ON documents(created_at);

-- Индекс по номеру документа, потенциально нужен для поиска по номеру
CREATE INDEX idx_documents_number ON documents(number);

```
