-- 0. Drop the new area_type column if it exists
ALTER TABLE place
    DROP COLUMN IF EXISTS area_type;

-- 1. Drop the foreign key constraint if it exists
ALTER TABLE place
    DROP CONSTRAINT IF EXISTS place_area_type_id_fkey;

-- 2. Add new area_type column if not exists
ALTER TABLE place
    ADD COLUMN IF NOT EXISTS area_type TEXT;

-- 3. Populate area_type from areatype.type_name if area_type_id exists
DO $$
    BEGIN
        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'place' AND column_name = 'area_type_id'
        ) THEN
            UPDATE place p
            SET area_type = a.type_name
            FROM areatype a
            WHERE p.area_type_id = a.id
              AND (p.area_type IS NULL OR p.area_type = '');
        END IF;
    END;
$$;

-- 4. Fill remaining nulls with a default value
UPDATE place
SET area_type = 'UNKNOWN'
WHERE area_type IS NULL;

-- 5. Set the column to NOT NULL
ALTER TABLE place
    ALTER COLUMN area_type SET NOT NULL;

-- 6. Drop the old foreign key column if it exists
ALTER TABLE place
    DROP COLUMN IF EXISTS area_type_id;
