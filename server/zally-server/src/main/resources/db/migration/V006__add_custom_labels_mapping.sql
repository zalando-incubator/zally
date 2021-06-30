ALTER TABLE api_review ADD custom_labels TEXT;

CREATE TABLE custom_label_mapping (
  api_review_id  BIGINT REFERENCES api_review (id),
  label_name     TEXT NOT NULL,
  label_value    TEXT NOT NULL
);