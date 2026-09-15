-- Entirely fictional fixtures. Stable IDs make startup repeatable without overwriting edits.
INSERT INTO region (id, region_name) VALUES
 ('10000000-0000-0000-0000-000000000001', 'Demo North'),
 ('10000000-0000-0000-0000-000000000002', 'Demo South') ON CONFLICT DO NOTHING;
INSERT INTO "Group" (id, name, region_id, is_fighting) VALUES
 ('20000000-0000-0000-0000-000000000001', 'Demo Alpha', '10000000-0000-0000-0000-000000000001', true),
 ('20000000-0000-0000-0000-000000000002', 'Demo Beta', '10000000-0000-0000-0000-000000000002', false) ON CONFLICT DO NOTHING;
INSERT INTO positions (id, position_name) VALUES ('30000000-0000-0000-0000-000000000001', 'Demo coordinator') ON CONFLICT DO NOTHING;
INSERT INTO unit (id, unit_name) VALUES ('40000000-0000-0000-0000-000000000001', 'Fictional operations unit') ON CONFLICT DO NOTHING;
INSERT INTO contractor (id, first_name, last_name, nick_name, gender, birth_date, nationality,
 date_of_arrival_to_unit, contractor_status, c_rank, position_id, unit_id, group_id, created_by, updated_by) VALUES
 ('50000000-0000-0000-0000-000000000001', 'Alex', 'Example', 'Demo-A', 'M', '1990-01-01', 'Demo', '2025-01-01', 'SERVICE', 'Demo rank', '30000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 'demo', 'demo'),
 ('50000000-0000-0000-0000-000000000002', 'Morgan', 'Sample', 'Demo-B', 'F', '1992-01-01', 'Demo', '2025-01-01', 'SERVICE', 'Demo rank', '30000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 'demo', 'demo'),
 ('50000000-0000-0000-0000-000000000003', 'Jamie', 'Placeholder', 'Demo-C', 'M', '1994-01-01', 'Demo', '2025-01-01', 'SERVICE', 'Demo rank', '30000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000002', 'demo', 'demo') ON CONFLICT DO NOTHING;
INSERT INTO place (id, name, county, district, region, area_type, region_id, coeficient) VALUES
 ('60000000-0000-0000-0000-000000000001', 'Demo Field', 'Example County', 'Example District', 'Demo North', 'COUNTY', '10000000-0000-0000-0000-000000000001', '100'),
 ('60000000-0000-0000-0000-000000000002', 'Demo Base', 'Example County', 'Example District', 'Demo North', 'COUNTY', '10000000-0000-0000-0000-000000000001', '30'),
 ('60000000-0000-0000-0000-000000000003', 'Demo Station', 'Example County', 'Example District', 'Demo South', 'COUNTY', '10000000-0000-0000-0000-000000000002', '30') ON CONFLICT DO NOTHING;
INSERT INTO descriptiontemplate (group_id, content, content_rest, created_by, updated_by) VALUES
 ('20000000-0000-0000-0000-000000000001', 'Synthetic field inspection completed.', 'Synthetic training and equipment checks.', 'demo', 'demo'),
 ('20000000-0000-0000-0000-000000000002', 'Synthetic maintenance completed.', 'Synthetic training and planning.', 'demo', 'demo') ON CONFLICT DO NOTHING;
INSERT INTO regionreport (id, report_date, region_id, region_description, arrived_contractors, departed_contractors, extra_data, created_by, updated_by) VALUES
 ('70000000-0000-0000-0000-000000000001', CURRENT_DATE, '10000000-0000-0000-0000-000000000001', 'Fictional daily operations: training and field inspection.', '{}', '{}', '{}', 'demo', 'demo'),
 ('70000000-0000-0000-0000-000000000002', CURRENT_DATE, '10000000-0000-0000-0000-000000000002', 'Fictional daily operations: maintenance and planning.', '{}', '{}', '{}', 'demo', 'demo') ON CONFLICT DO NOTHING;
INSERT INTO groupreport (id, group_id, region_report_id, description, fighting_contractors, rest_contractors, fighting_places, rest_places, report_date, worked, ammo_verified, ammunition, extra_data_group_report, created_by, updated_by) VALUES
 ('80000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', '70000000-0000-0000-0000-000000000001', 'Synthetic inspection of Demo Field; no real operational information.', '{50000000-0000-0000-0000-000000000001}', '{50000000-0000-0000-0000-000000000002}', '{60000000-0000-0000-0000-000000000001}', '{60000000-0000-0000-0000-000000000002}', CURRENT_DATE, true, true, '[]', '{}', 'demo', 'demo'),
 ('80000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002', '70000000-0000-0000-0000-000000000002', 'Synthetic maintenance of Demo Station.', '{}', '{50000000-0000-0000-0000-000000000003}', '{}', '{60000000-0000-0000-0000-000000000003}', CURRENT_DATE, false, false, '[]', '{}', 'demo', 'demo') ON CONFLICT DO NOTHING;
