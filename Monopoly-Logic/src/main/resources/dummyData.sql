INSERT INTO game (game_id, game_state, current_player_index)
VALUES ('550e8400-e29b-41d4-a716-446655440000', 'IN_PROGRESS', 0);

INSERT INTO player (player_id, name, balance, in_jail, position)
VALUES ('123e4567-e89b-12d3-a456-426614174000', 'PLAYER_RED', 1500, false, 0),
       ('123e4567-e89b-12d3-a456-426614174001', 'PLAYER_GREEN', 1500, false, 0),
       ('123e4567-e89b-12d3-a456-426614174002', 'PLAYER_YELLOW', 1500, false, 0),
       ('123e4567-e89b-12d3-a456-426614174003', 'PLAYER_BLUE', 1500, false, 0);

INSERT INTO game_players (game_game_id, players_player_id)
VALUES ('550e8400-e29b-41d4-a716-446655440000', '123e4567-e89b-12d3-a456-426614174000'),
       ('550e8400-e29b-41d4-a716-446655440000', '123e4567-e89b-12d3-a456-426614174001'),
       ('550e8400-e29b-41d4-a716-446655440000', '123e4567-e89b-12d3-a456-426614174002'),
       ('550e8400-e29b-41d4-a716-446655440000', '123e4567-e89b-12d3-a456-426614174003');

INSERT INTO player_owned_properties (player_player_id, property_name, cost, rent, upgradable)
VALUES ('123e4567-e89b-12d3-a456-426614174000', 'SHOULD_BE_ADDED', 400, 50, true),
       ('123e4567-e89b-12d3-a456-426614174001', 'SHOULD_BE_ADDED', 500, 60, true),
       ('123e4567-e89b-12d3-a456-426614174002', 'SHOULD_BE_ADDED', 400, 50, true),
       ('123e4567-e89b-12d3-a456-426614174003', 'SHOULD_BE_ADDED', 400, 50, true);
