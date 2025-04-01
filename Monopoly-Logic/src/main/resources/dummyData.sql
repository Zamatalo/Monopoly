INSERT INTO game (game_id, game_state, current_player_index)
VALUES ('550e8400-e29b-41d4-a716-446655440000', 'STARTED', 0);

INSERT INTO player (player_id, color, balance, in_jail, position)
VALUES ('123e4567-e89b-12d3-a456-426614174000', 'PLAYER_RED', 1000, false, 0),
       ('123e4567-e89b-12d3-a456-426614174001', 'PLAYER_GREEN', 1000, false, 0),
       ('123e4567-e89b-12d3-a456-426614174002', 'PLAYER_YELLOW', 1000, false, 0),
       ('123e4567-e89b-12d3-a456-426614174003', 'PLAYER_BLUE', 1000, false, 0);

INSERT INTO game_players (game_game_id, players_player_id)
VALUES ('550e8400-e29b-41d4-a716-446655440000', '123e4567-e89b-12d3-a456-426614174000'),
       ('550e8400-e29b-41d4-a716-446655440000', '123e4567-e89b-12d3-a456-426614174001'),
       ('550e8400-e29b-41d4-a716-446655440000', '123e4567-e89b-12d3-a456-426614174002'),
       ('550e8400-e29b-41d4-a716-446655440000', '123e4567-e89b-12d3-a456-426614174003');

INSERT INTO player_owned_properties (player_player_id, property_name, cost, rent, upgradable)
VALUES ('123e4567-e89b-12d3-a456-426614174000', 'BROWN_1', 60, 50, true),
       ('123e4567-e89b-12d3-a456-426614174000', 'BROWN_2', 90, 50, true),
       ('123e4567-e89b-12d3-a456-426614174001', 'LIGHTBLUE_1', 120, 60, true),
       ('123e4567-e89b-12d3-a456-426614174001', 'LIGHTBLUE_2', 130, 60, true),
       ('123e4567-e89b-12d3-a456-426614174001', 'LIGHTBLUE_3', 150, 60, true),
       ('123e4567-e89b-12d3-a456-426614174002', 'PINK_1', 140, 60, true),
       ('123e4567-e89b-12d3-a456-426614174002', 'PINK_2', 160, 60, true),
       ('123e4567-e89b-12d3-a456-426614174002', 'PINK_3', 140, 60, true),
       ('123e4567-e89b-12d3-a456-426614174003', 'ORANGE_1', 180, 60, true),
       ('123e4567-e89b-12d3-a456-426614174003', 'ORANGE_2', 200, 60, true),
       ('123e4567-e89b-12d3-a456-426614174003', 'ORANGE_3', 200, 60, true);
