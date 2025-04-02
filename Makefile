APP_IMAGE=workreportplus_app
POSTGRES_IMAGE=postgres:15.3
DB_VOLUME=db_data
BACKUP_FILE=db_data_backup.tar.gz

.PHONY: export-images import-images backup-db restore-db

# --- Export Images ---
export-images:
	docker save -o $(APP_IMAGE).tar $(APP_IMAGE)
	docker save -o postgres15.tar $(POSTGRES_IMAGE)

# --- Import Images ---
import-images:
	docker load -i $(APP_IMAGE).tar
	docker load -i postgres15.tar

# --- Backup DB Volume ---
backup-db:
	docker run --rm -v $(DB_VOLUME):/volume -v $$PWD:/backup alpine \
		tar czf /backup/$(BACKUP_FILE) -C /volume .

# --- Restore DB Volume ---
restore-db:
	docker volume create $(DB_VOLUME)
	docker run --rm -v $(DB_VOLUME):/volume -v $$PWD:/backup alpine \
		tar xzf /backup/$(BACKUP_FILE) -C /volume

