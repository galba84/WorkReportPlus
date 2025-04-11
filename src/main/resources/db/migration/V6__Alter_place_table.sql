-- 1. Drop the foreign key constraint if it exists
ALTER TABLE place
    DROP CONSTRAINT IF EXISTS place_area_type_id_fkey;

-- 2. Add new column (initially nullable)
ALTER TABLE place
    ADD COLUMN area_type TEXT;

-- 3. Populate area_type from areatype.type_name
UPDATE place p
SET area_type = a.type_name
FROM areatype a
WHERE p.area_type_id = a.id;

-- 4. Fill remaining nulls with a default value (optional but prevents NOT NULL error)
UPDATE place
SET area_type = 'UNKNOWN'
WHERE area_type IS NULL;

-- 5. Set the column to NOT NULL
ALTER TABLE place
    ALTER COLUMN area_type SET NOT NULL;

-- 6. Drop the old foreign key column
ALTER TABLE place
    DROP COLUMN area_type_id;
