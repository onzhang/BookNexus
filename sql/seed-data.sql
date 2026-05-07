-- ============================================================
-- BookNexus 图书管理系统 — 演示种子数据
-- 包含：全部 12 张业务表的演示数据
-- @author 张俊文  @since 2026-05-06
-- 使用方式：在 init.sql 之后执行
--   mysql -u root booknexus < sql/init.sql
--   mysql -u root booknexus < sql/seed-data.sql
-- ============================================================
USE booknexus;

-- ============================================================
-- 1. 用户数据（9 个测试用户）
-- 密码统一为 a123456（BCrypt 加密）
-- reader01 ~ reader08: 普通用户（ENABLED）
-- disabled_user: 禁用用户（DISABLED）
-- ============================================================
INSERT IGNORE INTO `user` (`id`, `username`, `password`, `email`, `phone`, `role`, `status`) VALUES
(2,  'reader01',       '$2b$10$zVbt8GcpDMc4T345twzJz.a5oHkrSBdj7UPaKOvwwtDMIfuc080ZK', 'reader01@booknexus.com',       NULL, 'USER', 'ENABLED'),
(3,  'reader02',       '$2b$10$zVbt8GcpDMc4T345twzJz.a5oHkrSBdj7UPaKOvwwtDMIfuc080ZK', 'reader02@booknexus.com',       NULL, 'USER', 'ENABLED'),
(4,  'reader03',       '$2b$10$zVbt8GcpDMc4T345twzJz.a5oHkrSBdj7UPaKOvwwtDMIfuc080ZK', 'reader03@booknexus.com',       NULL, 'USER', 'ENABLED'),
(5,  'reader04',       '$2b$10$zVbt8GcpDMc4T345twzJz.a5oHkrSBdj7UPaKOvwwtDMIfuc080ZK', 'reader04@booknexus.com',       NULL, 'USER', 'ENABLED'),
(6,  'reader05',       '$2b$10$zVbt8GcpDMc4T345twzJz.a5oHkrSBdj7UPaKOvwwtDMIfuc080ZK', 'reader05@booknexus.com',       NULL, 'USER', 'ENABLED'),
(7,  'reader06',       '$2b$10$zVbt8GcpDMc4T345twzJz.a5oHkrSBdj7UPaKOvwwtDMIfuc080ZK', 'reader06@booknexus.com',       NULL, 'USER', 'ENABLED'),
(8,  'reader07',       '$2b$10$zVbt8GcpDMc4T345twzJz.a5oHkrSBdj7UPaKOvwwtDMIfuc080ZK', 'reader07@booknexus.com',       NULL, 'USER', 'ENABLED'),
(9,  'reader08',       '$2b$10$zVbt8GcpDMc4T345twzJz.a5oHkrSBdj7UPaKOvwwtDMIfuc080ZK', 'reader08@booknexus.com',       NULL, 'USER', 'ENABLED'),
(10, 'disabled_user',  '$2b$10$zVbt8GcpDMc4T345twzJz.a5oHkrSBdj7UPaKOvwwtDMIfuc080ZK', 'disabled_user@booknexus.com',  NULL, 'USER', 'DISABLED');

-- ============================================================
-- 2. 书籍数据（50 本，中英混合）
-- 编号 1-30：中文书籍，编号 31-50：英文书籍
-- 热门书籍（stock=5）：活着、三体、Clean Code、Design Patterns、1984
-- 特殊状态：Sapiens(DAMAGED)、Outliers(LOST)
-- ============================================================
INSERT IGNORE INTO `book` (`id`, `title`, `author`, `isbn`, `publisher`, `publish_date`, `description`, `cover_url`, `stock`, `available_stock`, `status`, `bookshelf_id`) VALUES
-- 书架 1（ID 1-5）
(1,  '活着',            '余华',                    '978-7-02-013281-8',  '作家出版社',           '2012-01-01', '讲述了一个普通人在历史洪流中历经苦难、顽强生存的故事，深刻展现了生命的韧性与意义。',           '/covers/活着.svg',            5, 5, 'AVAILABLE', 1),
(2,  '百年孤独',        '加西亚·马尔克斯',          '978-7-5442-5399-4',  '南海出版公司',         '2011-01-01', '魔幻现实主义经典之作，讲述了布恩迪亚家族七代人的兴衰传奇。',                                   '/covers/百年孤独.jpg',        1, 1, 'AVAILABLE', 1),
(3,  '红楼梦',          '曹雪芹',                  '978-7-02-000220-7',  '人民文学出版社',        '2018-01-01', '中国古典文学巅峰之作，以贾宝玉与林黛玉的爱情悲剧为主线，描绘了大家族的兴衰。',                 '/covers/红楼梦.svg',          1, 1, 'AVAILABLE', 1),
(4,  '围城',            '钱钟书',                  '978-7-02-002475-9',  '人民文学出版社',        '2017-01-01', '以幽默讽刺的笔触描写了知识分子方鸿渐在爱情与事业中的围城困境。',                               '/covers/围城.jpg',            1, 1, 'AVAILABLE', 1),
(5,  '平凡的世界',      '路遥',                    '978-7-5302-0955-4',  '北京十月文艺出版社',    '2012-01-01', '以孙少安和孙少平两兄弟的奋斗历程，展现了中国改革开放初期普通人的命运变迁。',                   '/covers/平凡的世界.jpg',      1, 1, 'AVAILABLE', 1),
-- 书架 2（ID 6-10）
(6,  '三体',            '刘慈欣',                  '978-7-5366-6163-4',  '重庆出版社',           '2008-01-01', '讲述人类文明与三体文明之间的首次接触，开启了一段宏大的宇宙史诗。',                             '/covers/三体.svg',            5, 5, 'AVAILABLE', 2),
(7,  '三体II：黑暗森林', '刘慈欣',                  '978-7-5366-6641-9',  '重庆出版社',           '2008-01-01', '三体系列第二部，揭示了宇宙文明的黑暗森林法则。',                                               '/covers/三体II：黑暗森林.svg', 1, 1, 'AVAILABLE', 2),
(8,  '三体III：死神永生', '刘慈欣',                 '978-7-5366-6936-6',  '重庆出版社',           '2010-01-01', '三体系列最终章，探讨了宇宙的终极命运与文明的存亡。',                                           '/covers/三体III：死神永生.svg', 1, 1, 'AVAILABLE', 2),
(9,  '白夜行',          '东野圭吾',                 '978-7-5442-9114-9',  '南海出版公司',         '2013-01-01', '一桩命案牵出两个孩子的悲惨命运，揭示了人性深处的光明与黑暗。',                                 '/covers/白夜行.svg',          1, 1, 'AVAILABLE', 2),
(10, '小王子',          '安托万·德·圣-埃克苏佩里',  '978-7-02-013918-3',  '人民文学出版社',        '2018-01-01', '一个来自外星球的小王子的奇妙旅行，关于爱与责任的心灵寓言。',                                   '/covers/小王子.svg',          1, 1, 'AVAILABLE', 2),
-- 书架 3（ID 11-15）
(11, '月亮与六便士',    '毛姆',                    '978-7-5327-7806-0',  '上海译文出版社',        '2018-01-01', '以画家高更为原型，讲述了一个中年男人为追求艺术梦想抛弃一切的动人故事。',                       '/covers/月亮与六便士.svg',    1, 1, 'AVAILABLE', 3),
(12, '人间失格',        '太宰治',                  '978-7-5063-9823-0',  '作家出版社',           '2015-01-01', '太宰治半自传体小说，描绘了主人公在人间挣扎求存的悲剧人生。',                                   '/covers/人间失格.svg',        1, 1, 'AVAILABLE', 3),
(13, '代码整洁之道',    'Robert C. Martin',        '978-7-115-51223-2',  '人民邮电出版社',        '2020-01-01', '软件工程经典之作，指导程序员如何编写整洁、可维护的代码。',                                     '/covers/代码整洁之道.svg',    1, 1, 'AVAILABLE', 3),
(14, '人月神话',        'Frederick P. Brooks Jr.', '978-7-302-36086-6',  '清华大学出版社',        '2015-01-01', '软件项目管理经典著作，探讨了大型软件系统的开发与管理难题。',                                   '/covers/人月神话.svg',        1, 1, 'AVAILABLE', 3),
(15, '黑客与画家',      'Paul Graham',             '978-7-115-46464-2',  '人民邮电出版社',        '2019-01-01', '硅谷创业教父保罗·格雷厄姆的随笔集，探讨编程、创业与科技的未来。',                               '/covers/黑客与画家.svg',      1, 1, 'AVAILABLE', 3),
-- 书架 4（ID 16-20）
(16, '浪潮之巅',        '吴军',                    '978-7-115-30155-2',  '人民邮电出版社',        '2019-01-01', '详细记录了IT产业各巨头的发展历程与兴衰规律。',                                                 '/covers/浪潮之巅.svg',        1, 1, 'AVAILABLE', 4),
(17, '数学之美',        '吴军',                    '978-7-115-32352-1',  '人民邮电出版社',        '2020-01-01', '用通俗易懂的语言揭示数学在信息技术中的美妙应用。',                                             '/covers/数学之美.svg',        1, 1, 'AVAILABLE', 4),
(18, '万历十五年',      '黄仁宇',                  '978-7-101-10735-9',  '中华书局',             '2014-01-01', '以1587年为切面，剖析明朝中晚期的政治、经济与社会困境。',                                       '/covers/万历十五年.svg',      1, 1, 'AVAILABLE', 4),
(19, '人类简史',        '尤瓦尔·赫拉利',           '978-7-5086-9607-0',  '中信出版社',           '2017-01-01', '从认知革命到人工智能，全面回顾了人类发展历史的关键节点。',                                     '/covers/人类简史.svg',        1, 1, 'AVAILABLE', 4),
(20, '枪炮、病菌与钢铁','Jared Diamond',           '978-7-5086-6677-0',  '中信出版社',           '2016-01-01', '从地理和生物角度揭示了不同文明发展差异的深层原因。',                                           '/covers/枪炮、病菌与钢铁.svg', 1, 1, 'AVAILABLE', 4),
-- 书架 5（ID 21-25）
(21, '挪威的森林',      '村上春树',                '978-7-5327-6769-6',  '上海译文出版社',        '2018-01-01', '讲述了一段发生在1960年代日本的青春爱情故事，充满感伤与回忆。',                                 '/covers/挪威的森林.svg',      1, 1, 'AVAILABLE', 5),
(22, '解忧杂货店',      '东野圭吾',                '978-7-5442-7087-0',  '南海出版公司',         '2014-01-01', '一家神奇的杂货店连接着过去与现在，为人们解答人生的困惑。',                                     '/covers/解忧杂货店.jpg',      1, 1, 'AVAILABLE', 5),
(23, '追风筝的人',      '卡勒德·胡赛尼',           '978-7-208-06164-4',  '上海人民出版社',        '2006-01-01', '关于友谊、背叛与救赎的感人故事，以阿富汗战乱为背景。',                                         '/covers/追风筝的人.jpg',      1, 1, 'AVAILABLE', 5),
(24, '局外人',          '加缪',                    '978-7-5327-6614-6',  '上海译文出版社',        '2016-01-01', '加缪存在主义代表作，讲述了一个对世间一切漠然处之的"局外人"的故事。',                            '/covers/局外人.svg',          1, 1, 'AVAILABLE', 5),
(25, '苏菲的世界',      '乔斯坦·贾德',             '978-7-5063-9698-1',  '作家出版社',           '2019-01-01', '以小说的形式讲述了西方哲学的发展历程，是哲学入门经典之作。',                                   '/covers/苏菲的世界.svg',      1, 1, 'AVAILABLE', 5),
-- 书架 6（ID 26-30）
(26, '乌合之众',        '古斯塔夫·勒庞',           '978-7-5117-4852-5',  '中央编译出版社',        '2014-01-01', '群体心理学经典著作，剖析了群体行为的特点与形成机制。',                                         '/covers/乌合之众.svg',        1, 1, 'AVAILABLE', 6),
(27, '原则',            'Ray Dalio',               '978-7-5086-9628-4',  '中信出版社',           '2018-01-01', '桥水基金创始人瑞·达利欧分享其在生活与工作中遵循的原则。',                                     '/covers/原则.svg',            1, 1, 'AVAILABLE', 6),
(28, '思考，快与慢',    'Daniel Kahneman',         '978-7-5086-4410-4',  '中信出版社',           '2012-01-01', '诺贝尔经济学奖得主卡尼曼揭示人类思维的两个系统及其决策偏差。',                                 '/covers/思考，快与慢.svg',    1, 1, 'AVAILABLE', 6),
(29, '看见',            '柴静',                    '978-7-5633-0249-X',  '广西师范大学出版社',    '2013-01-01', '前央视记者柴静的自传性作品，记录了中国社会十年间的重大事件与个人思考。',                       '/covers/看见.svg',            1, 1, 'AVAILABLE', 6),
(30, '送你一颗子弹',    '刘瑜',                    '978-7-5426-2586-8',  '上海三联书店',         '2010-01-01', '刘瑜的随笔集，以幽默犀利的笔触探讨社会、文化与生活。',                                         '/covers/送你一颗子弹.svg',    1, 1, 'AVAILABLE', 6),
-- 书架 7（ID 31-35）
(31, 'Clean Code',                     'Robert C. Martin',                 '978-0-13-235088-4',  'Prentice Hall',   '2008-01-01', 'A landmark book in software engineering, teaching developers how to write clean, maintainable, and efficient code.',         '/covers/Clean Code.jpg',                  5, 5, 'AVAILABLE', 7),
(32, 'Design Patterns',                'Erich Gamma et al.',              '978-0-201-63361-0',  'Addison-Wesley',  '1994-01-01', 'The classic reference for object-oriented design patterns, cataloging 23 proven solutions to common software design problems.', '/covers/Design Patterns.jpg',             5, 5, 'AVAILABLE', 7),
(33, 'The Pragmatic Programmer',       'David Thomas, Andrew Hunt',       '978-0-13-595705-9',  'Addison-Wesley',  '2019-01-01', 'A guide to becoming a better programmer through practical advice on software craftsmanship and career development.',            '/covers/The Pragmatic Programmer.jpg',    1, 1, 'AVAILABLE', 7),
(34, 'Refactoring',                    'Martin Fowler',                   '978-0-13-475759-9',  'Addison-Wesley',  '2018-01-01', 'A comprehensive guide to improving existing codebases through safe and systematic refactoring techniques.',                       '/covers/Refactoring.jpg',                 1, 1, 'AVAILABLE', 7),
(35, 'Introduction to Algorithms',     'Thomas H. Cormen et al.',         '978-0-262-03384-8',  'MIT Press',       '2009-01-01', 'The definitive textbook on algorithms and data structures, covering fundamental to advanced topics in computer science.',         '/covers/Introduction to Algorithms.jpg',  1, 1, 'AVAILABLE', 7),
-- 书架 8（ID 36-40）
(36, 'Structure and Interpretation of Computer Programs', 'Harold Abelson et al.',       '978-0-262-51087-5',  'MIT Press',       '1996-01-01', 'A foundational text in computer science that teaches the principles of programming language design and computation.',               '/covers/Structure and Interpretation of Computer Programs.jpg',                         1, 1, 'AVAILABLE', 8),
(37, 'Artificial Intelligence: A Modern Approach',       'Stuart Russell, Peter Norvig', '978-0-13-461099-3',  'Pearson',        '2020-01-01', 'The leading textbook on artificial intelligence, covering intelligent agents, search, planning, and machine learning.',             '/covers/Artificial Intelligence - A Modern Approach.jpg',           1, 1, 'AVAILABLE', 8),
(38, 'Computer Networking',                              'James F. Kurose, Keith W. Ross','978-0-13-359414-0',  'Pearson',        '2017-01-01', 'A comprehensive introduction to computer networking, covering protocols, architecture, and network applications.',                  '/covers/Computer Networking.jpg',          1, 1, 'AVAILABLE', 8),
(39, 'Operating System Concepts',                        'Abraham Silberschatz et al.',  '978-1-119-32091-3',  'Wiley',          '2018-01-01', 'The standard textbook on operating systems, covering process management, memory management, storage, and security.',                '/covers/Operating System Concepts.jpg',    1, 1, 'AVAILABLE', 8),
(40, 'Database System Concepts',                         'Abraham Silberschatz et al.',  '978-0-07-802215-9',  'McGraw-Hill',    '2019-01-01', 'A thorough introduction to database systems, covering relational models, SQL, storage, indexing, and transaction processing.',      '/covers/Database System Concepts.jpg',     1, 1, 'AVAILABLE', 8),
-- 书架 9（ID 41-45）
(41, 'To Kill a Mockingbird',  'Harper Lee',            '978-0-06-112008-4',  'HarperCollins',    '2006-01-01', 'A powerful story of racial injustice and moral growth in the American South, told through the eyes of young Scout Finch.',       '/covers/To Kill a Mockingbird.jpg',  1, 1, 'AVAILABLE', 9),
(42, '1984',                   'George Orwell',          '978-0-451-52493-5',  'Signet Classics',  '1961-01-01', 'A dystopian masterpiece depicting a totalitarian world where independent thought is suppressed and history is rewritten.',        '/covers/1984.svg',                    5, 5, 'AVAILABLE', 9),
(43, 'Animal Farm',            'George Orwell',          '978-0-451-52634-2',  'Signet Classics',  '1996-01-01', 'A satirical allegory about a group of farm animals who overthrow their human farmer, only to be ruled by more oppressive pigs.',  '/covers/Animal Farm.jpg',             1, 1, 'AVAILABLE', 9),
(44, 'The Great Gatsby',       'F. Scott Fitzgerald',    '978-0-7432-7356-5',  'Scribner',         '2004-01-01', 'A story of wealth, love, and the American Dream set in the Roaring Twenties, centered on the mysterious Jay Gatsby.',             '/covers/The Great Gatsby.svg',        1, 1, 'AVAILABLE', 9),
(45, 'Pride and Prejudice',    'Jane Austen',            '978-0-14-143951-8',  'Penguin Classics', '2002-01-01', 'A timeless romance exploring manners, marriage, and social class in 19th-century England.',                                        '/covers/Pride and Prejudice.jpg',     1, 1, 'AVAILABLE', 9),
-- 书架 10（ID 46-50）
(46, 'The Catcher in the Rye',  'J.D. Salinger',         '978-0-316-76948-0',  'Little, Brown',           '1991-01-01', 'A story of teenage alienation and rebellion, following Holden Caulfield\'s journey through New York City.',                        '/covers/The Catcher in the Rye.jpg',  1, 1, 'AVAILABLE', 10),
(47, 'Thinking, Fast and Slow', 'Daniel Kahneman',       '978-0-374-53355-7',  'Farrar, Straus and Giroux','2013-01-01', 'An exploration of the two systems that drive the human mind — fast intuitive thinking and slow deliberate reasoning.',              '/covers/Thinking, Fast and Slow.svg',  1, 1, 'AVAILABLE', 10),
(48, 'Sapiens',                 'Yuval Noah Harari',     '978-0-06-231609-7',  'Harper',                  '2015-01-01', 'A sweeping history of humankind from the Stone Age to the modern age, exploring how Homo sapiens came to dominate the planet.',   '/covers/Sapiens.jpg',                  1, 0, 'DAMAGED', 10),
(49, 'Outliers',                'Malcolm Gladwell',      '978-0-316-01793-0',  'Little, Brown',           '2011-01-01', 'An examination of what makes high-achievers different, exploring the hidden advantages of culture, timing, and opportunity.',     '/covers/Outliers.svg',                 1, 0, 'LOST',    10),
(50, 'The Tipping Point',       'Malcolm Gladwell',      '978-0-316-31696-5',  'Little, Brown',           '2002-01-01', 'An analysis of how small actions can trigger major social changes, exploring the tipping points of trends and epidemics.',         '/covers/The Tipping Point.jpg',        1, 1, 'AVAILABLE', 10);

-- ============================================================
-- 3. 书籍-类别关联数据（book_category_rel）
-- 每本书 1-2 个类别，热门书籍（stock=5）关联 2 个类别
-- ============================================================
INSERT IGNORE INTO `book_category_rel` (`book_id`, `category_id`) VALUES
-- 热门书籍：每本 2 个类别
(1, 1),   -- 活着 → 文学
(1, 10),  -- 活着 → 其他
(6, 2),   -- 三体 → 科学
(6, 1),   -- 三体 → 文学
(31, 3),  -- Clean Code → 技术
(31, 4),  -- Clean Code → 教育
(32, 3),  -- Design Patterns → 技术
(32, 4),  -- Design Patterns → 教育
(42, 1),  -- 1984 → 文学
(42, 6),  -- 1984 → 哲学
-- 中文书籍
(2, 1),   -- 百年孤独 → 文学
(3, 1),   -- 红楼梦 → 文学
(4, 1),   -- 围城 → 文学
(5, 1),   -- 平凡的世界 → 文学
(7, 2),   -- 三体II：黑暗森林 → 科学
(8, 2),   -- 三体III：死神永生 → 科学
(9, 1),   -- 白夜行 → 文学
(10, 1),  -- 小王子 → 文学
(11, 1),  -- 月亮与六便士 → 文学
(11, 7),  -- 月亮与六便士 → 艺术
(12, 1),  -- 人间失格 → 文学
(12, 6),  -- 人间失格 → 哲学
(13, 3),  -- 代码整洁之道 → 技术
(14, 3),  -- 人月神话 → 技术
(15, 3),  -- 黑客与画家 → 技术
(15, 7),  -- 黑客与画家 → 艺术
(16, 3),  -- 浪潮之巅 → 技术
(16, 8),  -- 浪潮之巅 → 经济
(17, 2),  -- 数学之美 → 科学
(17, 4),  -- 数学之美 → 教育
(18, 5),  -- 万历十五年 → 历史
(19, 2),  -- 人类简史 → 科学
(19, 5),  -- 人类简史 → 历史
(20, 2),  -- 枪炮、病菌与钢铁 → 科学
(20, 5),  -- 枪炮、病菌与钢铁 → 历史
(21, 1),  -- 挪威的森林 → 文学
(22, 1),  -- 解忧杂货店 → 文学
(23, 1),  -- 追风筝的人 → 文学
(24, 1),  -- 局外人 → 文学
(24, 6),  -- 局外人 → 哲学
(25, 1),  -- 苏菲的世界 → 文学
(25, 6),  -- 苏菲的世界 → 哲学
(26, 8),  -- 乌合之众 → 经济
(27, 8),  -- 原则 → 经济
(27, 4),  -- 原则 → 教育
(28, 6),  -- 思考，快与慢 → 哲学
(29, 1),  -- 看见 → 文学
(30, 1),  -- 送你一颗子弹 → 文学
-- 英文书籍
(33, 3),  -- The Pragmatic Programmer → 技术
(34, 3),  -- Refactoring → 技术
(35, 3),  -- Introduction to Algorithms → 技术
(36, 3),  -- SICP → 技术
(37, 3),  -- AI: A Modern Approach → 技术
(38, 3),  -- Computer Networking → 技术
(39, 3),  -- OS Concepts → 技术
(40, 3),  -- Database System Concepts → 技术
(41, 1),  -- To Kill a Mockingbird → 文学
(43, 1),  -- Animal Farm → 文学
(44, 1),  -- The Great Gatsby → 文学
(45, 1),  -- Pride and Prejudice → 文学
(46, 1),  -- The Catcher in the Rye → 文学
(47, 6),  -- Thinking, Fast and Slow → 哲学
(48, 2),  -- Sapiens → 科学
(49, 1),  -- Outliers → 文学
(49, 8),  -- Outliers → 经济
(50, 1),  -- The Tipping Point → 文学
(50, 8);  -- The Tipping Point → 经济

-- ============================================================
-- 4. 借阅记录（25 条，覆盖全部 6 种状态）
-- 日期统一使用相对函数（DATE_SUB / CURDATE），确保逾期始终有效
-- 热门书籍：三体(ID=6) 4 条、活着(ID=1) 4 条
-- reader01(ID=2) 借满 5 本上限（BORROWED/RENEWED）
-- 归还可产生逾期罚款 0.50 / 目前在借逾期罚款累计至 1.50
-- ============================================================
INSERT IGNORE INTO `borrow_record` (`user_id`, `book_id`, `borrow_date`, `due_date`, `return_date`, `status`, `reject_reason`, `renew_count`, `fine_amount`) VALUES
-- PENDING（2 条等待审批）
(2, 3,  DATE_SUB(CURDATE(), INTERVAL 2 DAY),  DATE_SUB(CURDATE(), INTERVAL 2 DAY) + INTERVAL 30 DAY,  NULL, 'PENDING',  NULL, 0, 0.00),
(3, 21, DATE_SUB(CURDATE(), INTERVAL 1 DAY),  DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 30 DAY,  NULL, 'PENDING',  NULL, 0, 0.00),
-- APPROVED（2 条已批准待出库）
(3, 2,  DATE_SUB(CURDATE(), INTERVAL 5 DAY),  DATE_SUB(CURDATE(), INTERVAL 5 DAY) + INTERVAL 30 DAY,  NULL, 'APPROVED', NULL, 0, 0.00),
(4, 4,  DATE_SUB(CURDATE(), INTERVAL 3 DAY),  DATE_SUB(CURDATE(), INTERVAL 3 DAY) + INTERVAL 30 DAY,  NULL, 'APPROVED', NULL, 0, 0.00),
-- BORROWED（6 条借阅中，含 2 条逾期）
(2, 6,  DATE_SUB(CURDATE(), INTERVAL 15 DAY), DATE_SUB(CURDATE(), INTERVAL 15 DAY) + INTERVAL 30 DAY, NULL, 'BORROWED', NULL, 0, 0.00),
(2, 31, DATE_SUB(CURDATE(), INTERVAL 12 DAY), DATE_SUB(CURDATE(), INTERVAL 12 DAY) + INTERVAL 30 DAY, NULL, 'BORROWED', NULL, 0, 0.00),
(2, 42, DATE_SUB(CURDATE(), INTERVAL 10 DAY), DATE_SUB(CURDATE(), INTERVAL 10 DAY) + INTERVAL 30 DAY, NULL, 'BORROWED', NULL, 0, 0.00),
(2, 10, DATE_SUB(CURDATE(), INTERVAL 8 DAY),  DATE_SUB(CURDATE(), INTERVAL 8 DAY) + INTERVAL 30 DAY,  NULL, 'BORROWED', NULL, 0, 0.00),
-- ▼ 逾期 1：超期 10 天（到期日距今 10 天前），罚款 1.00 元
(5, 6,  DATE_SUB(CURDATE(), INTERVAL 40 DAY), DATE_SUB(CURDATE(), INTERVAL 10 DAY),                   NULL, 'BORROWED', NULL, 0, 1.00),
-- ▼ 逾期 2：超期 15 天（到期日距今 15 天前），罚款 1.50 元
(6, 1,  DATE_SUB(CURDATE(), INTERVAL 45 DAY), DATE_SUB(CURDATE(), INTERVAL 15 DAY),                   NULL, 'BORROWED', NULL, 0, 1.50),
-- RENEWED（2 条已续借）
(2, 32, DATE_SUB(CURDATE(), INTERVAL 35 DAY), DATE_SUB(CURDATE(), INTERVAL 35 DAY) + INTERVAL 30 DAY + INTERVAL 15 DAY, NULL, 'RENEWED', NULL, 1, 0.00),
-- ▼ 续借后逾期：到期日距今 5 天前，罚款 0.50 元
(7, 6,  DATE_SUB(CURDATE(), INTERVAL 50 DAY), DATE_SUB(CURDATE(), INTERVAL 5 DAY),                    NULL, 'RENEWED', NULL, 1, 0.50),
-- RETURNED（11 条已归还，含 2 条逾期归还）
(3, 1,  DATE_SUB(CURDATE(), INTERVAL 60 DAY), DATE_SUB(CURDATE(), INTERVAL 30 DAY), DATE_SUB(CURDATE(), INTERVAL 28 DAY), 'RETURNED', NULL, 0, 0.00),
-- ▼ 超期 5 天归还，罚款 0.50 元
(4, 1,  DATE_SUB(CURDATE(), INTERVAL 90 DAY), DATE_SUB(CURDATE(), INTERVAL 60 DAY), DATE_SUB(CURDATE(), INTERVAL 55 DAY), 'RETURNED', NULL, 0, 0.50),
(3, 6,  DATE_SUB(CURDATE(), INTERVAL 80 DAY), DATE_SUB(CURDATE(), INTERVAL 50 DAY), DATE_SUB(CURDATE(), INTERVAL 48 DAY), 'RETURNED', NULL, 0, 0.00),
(5, 42, DATE_SUB(CURDATE(), INTERVAL 70 DAY), DATE_SUB(CURDATE(), INTERVAL 40 DAY), DATE_SUB(CURDATE(), INTERVAL 38 DAY), 'RETURNED', NULL, 0, 0.00),
(6, 31, DATE_SUB(CURDATE(), INTERVAL 65 DAY), DATE_SUB(CURDATE(), INTERVAL 35 DAY), DATE_SUB(CURDATE(), INTERVAL 33 DAY), 'RETURNED', NULL, 0, 0.00),
(7, 32, DATE_SUB(CURDATE(), INTERVAL 55 DAY), DATE_SUB(CURDATE(), INTERVAL 25 DAY), DATE_SUB(CURDATE(), INTERVAL 22 DAY), 'RETURNED', NULL, 0, 0.00),
-- ▼ 超期 5 天归还，罚款 0.50 元
(8, 5,  DATE_SUB(CURDATE(), INTERVAL 100 DAY), DATE_SUB(CURDATE(), INTERVAL 70 DAY), DATE_SUB(CURDATE(), INTERVAL 65 DAY), 'RETURNED', NULL, 0, 0.50),
-- 续借 1 次后按期归还（无罚款）
(4, 9,  DATE_SUB(CURDATE(), INTERVAL 120 DAY), DATE_SUB(CURDATE(), INTERVAL 90 DAY), DATE_SUB(CURDATE(), INTERVAL 85 DAY), 'RETURNED', NULL, 1, 0.00),
-- REJECTED（2 条被拒绝）
(3, 1,  DATE_SUB(CURDATE(), INTERVAL 4 DAY),  DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL 30 DAY,  NULL, 'REJECTED', '您已有该书的历史借阅记录，请优先借阅其他未读书籍', 0, 0.00),
(8, 48, DATE_SUB(CURDATE(), INTERVAL 3 DAY),  DATE_SUB(CURDATE(), INTERVAL 3 DAY) + INTERVAL 30 DAY,  NULL, 'REJECTED', '该书目前处于损坏状态，暂时无法借阅',            0, 0.00),
-- 额外 RETURNED（凑足 25 条）
(9, 15, DATE_SUB(CURDATE(), INTERVAL 75 DAY), DATE_SUB(CURDATE(), INTERVAL 45 DAY), DATE_SUB(CURDATE(), INTERVAL 43 DAY), 'RETURNED', NULL, 0, 0.00),
(8, 33, DATE_SUB(CURDATE(), INTERVAL 40 DAY), DATE_SUB(CURDATE(), INTERVAL 10 DAY), DATE_SUB(CURDATE(), INTERVAL 8 DAY),  'RETURNED', NULL, 0, 0.00),
(2, 41, DATE_SUB(CURDATE(), INTERVAL 85 DAY), DATE_SUB(CURDATE(), INTERVAL 55 DAY), DATE_SUB(CURDATE(), INTERVAL 52 DAY), 'RETURNED', NULL, 0, 0.00);

-- ============================================================
-- 库存更新：根据当前在借记录（BORROWED / RENEWED / APPROVED）修正库存
-- available_stock = stock - 当前在借数
-- 在借数降至 0 时同步更新 status → BORROWED
-- 不修改 DAMAGED(Sapiens, ID=48) / LOST(Outliers, ID=49)
-- ============================================================
-- Book ID:6 (三体) 3 条在借（rec5 BORROWED, rec9 BORROWED, rec12 RENEWED），stock=5 → available_stock=2
UPDATE `book` SET `available_stock` = 2, `status` = 'AVAILABLE' WHERE `id` = 6;
-- Book ID:1 (活着) 1 条在借（rec10 BORROWED），stock=5 → available_stock=4
UPDATE `book` SET `available_stock` = 4, `status` = 'AVAILABLE' WHERE `id` = 1;
-- Book ID:31 (Clean Code) 1 条在借（rec6 BORROWED），stock=5 → available_stock=4
UPDATE `book` SET `available_stock` = 4, `status` = 'AVAILABLE' WHERE `id` = 31;
-- Book ID:32 (Design Patterns) 1 条在借（rec11 RENEWED），stock=5 → available_stock=4
UPDATE `book` SET `available_stock` = 4, `status` = 'AVAILABLE' WHERE `id` = 32;
-- Book ID:42 (1984) 1 条在借（rec7 BORROWED），stock=5 → available_stock=4
UPDATE `book` SET `available_stock` = 4, `status` = 'AVAILABLE' WHERE `id` = 42;
-- Book ID:2 (百年孤独) 1 条在借（rec3 APPROVED），stock=1 → available_stock=0 → BORROWED
UPDATE `book` SET `available_stock` = 0, `status` = 'BORROWED' WHERE `id` = 2;
-- Book ID:4 (围城) 1 条在借（rec4 APPROVED），stock=1 → available_stock=0 → BORROWED
UPDATE `book` SET `available_stock` = 0, `status` = 'BORROWED' WHERE `id` = 4;
-- Book ID:10 (小王子) 1 条在借（rec8 BORROWED），stock=1 → available_stock=0 → BORROWED
UPDATE `book` SET `available_stock` = 0, `status` = 'BORROWED' WHERE `id` = 10;

-- ============================================================
-- 5. 用户收藏数据（20 条）
-- 热门书籍（三体/活着/Clean Code/1984/Design Patterns）获得更多收藏
-- 每个用户 2-3 个收藏，无重复 (user_id, book_id)
-- ============================================================
INSERT IGNORE INTO `favorite` (`user_id`, `book_id`) VALUES
-- reader01（ID=2）：收藏 3 本热门书
(2, 6),   -- 三体
(2, 31),  -- Clean Code
(2, 42),  -- 1984
-- reader02（ID=3）：收藏 2 本
(3, 6),   -- 三体
(3, 32),  -- Design Patterns
-- reader03（ID=4）：收藏 3 本
(4, 1),   -- 活着
(4, 9),   -- 白夜行
(4, 6),   -- 三体
-- reader04（ID=5）：收藏 3 本
(5, 31),  -- Clean Code
(5, 42),  -- 1984
(5, 33),  -- The Pragmatic Programmer
-- reader05（ID=6）：收藏 3 本
(6, 1),   -- 活着
(6, 6),   -- 三体
(6, 22),  -- 解忧杂货店
-- reader06（ID=7）：收藏 2 本
(7, 32),  -- Design Patterns
(7, 15),  -- 黑客与画家
-- reader07（ID=8）：收藏 2 本
(8, 42),  -- 1984
(8, 5),   -- 平凡的世界
-- reader08（ID=9）：收藏 2 本
(9, 31),  -- Clean Code
(9, 1);   -- 活着

-- ============================================================
-- 6. 图书订阅数据（9 条）
-- 订阅焦点为当前在借的热门书籍（用户等待归还后借阅）
-- 含 2 条已取消订阅（is_active=0）
-- 无重复 (user_id, book_id)
-- ============================================================
INSERT IGNORE INTO `subscription` (`user_id`, `book_id`, `is_active`) VALUES
-- 活跃订阅（7 条）：等待热门书籍归还
(4, 2,  1),  -- reader03 订阅 百年孤独（当前 APPROVED）
(6, 4,  1),  -- reader05 订阅 围城（当前 APPROVED）
(7, 6,  1),  -- reader06 订阅 三体（3 条在借，热门）
(8, 1,  1),  -- reader07 订阅 活着（1 条在借）
(9, 42, 1),  -- reader08 订阅 1984（1 条在借）
(5, 31, 1),  -- reader04 订阅 Clean Code（1 条在借）
(2, 6,  1),  -- reader01 订阅 三体（3 条在借）
-- 已取消订阅（2 条）：曾订阅但已取消
(2, 32, 0),  -- reader01 曾订阅 Design Patterns（已取消）
(3, 10, 0);  -- reader02 曾订阅 小王子（已取消）

-- ============================================================
-- 7. 通知数据（13 条，覆盖 3 种类型）
-- 类型：SYSTEM(5) + OVERDUE(4) + SUBSCRIPTION(4)
-- 含已读(is_read=1)和未读(is_read=0)混合
-- ============================================================
INSERT IGNORE INTO `notification` (`user_id`, `type`, `title`, `content`, `is_read`) VALUES
-- SYSTEM 通知（5 条）
(2,  'SYSTEM',      '欢迎使用 BookNexus 图书管理系统',               '尊敬的读者，欢迎加入 BookNexus！您可以浏览馆藏书籍、借阅图书、收藏感兴趣的书籍。如有任何问题，请随时联系我们。',                                                                        1),
(3,  'SYSTEM',      '系统维护通知',                                 '系统将于本周六凌晨 2:00-4:00 进行例行维护，届时部分功能可能暂时不可用。给您带来的不便敬请谅解。',                                                                                    0),
(4,  'SYSTEM',      '新书上架通知',                                 '图书馆新增了一批热门书籍，包括《代码整洁之道》《Design Patterns》等经典技术书籍，欢迎前来借阅！',                                                                                        0),
(5,  'SYSTEM',      '借阅规则更新',                                 '为提升服务质量，图书馆更新了借阅规则：每人最多可借 5 本书，借阅期限为 30 天，逾期罚款 0.1 元/天。',                                                                                    1),
(6,  'SYSTEM',      '年度阅读报告已发布',                           '您的年度阅读报告已经生成，请登录系统查看您这一年的阅读记录和统计数据。',                                                                                                            0),
-- OVERDUE 通知（4 条）—— 匹配逾期借阅记录
(5,  'OVERDUE',     '逾期还书提醒',                                 '您借阅的《三体》已逾期10天，请尽快归还以避免产生更多罚款。当前逾期费用：1.00元。',                                                                                                    0),
(6,  'OVERDUE',     '逾期还书提醒',                                 '您借阅的《活着》已逾期15天，请尽快归还以避免产生更多罚款。当前逾期费用：1.50元。',                                                                                                    0),
(7,  'OVERDUE',     '逾期还书提醒',                                 '您续借的《三体》已逾期5天，请尽快归还。当前逾期费用：0.50元。',                                                                                                                         0),
(2,  'OVERDUE',     '借阅即将到期提醒',                             '您借阅的多本书籍将在未来一周内到期，请合理安排阅读时间或申请续借。',                                                                                                                  1),
-- SUBSCRIPTION 通知（4 条）
(3,  'SUBSCRIPTION','订阅书籍可借通知',                             '您订阅的《百年孤独》已有可借库存，欢迎前来借阅！',                                                                                                                                      0),
(6,  'SUBSCRIPTION','订阅书籍可借通知',                             '您订阅的《围城》现在可以借阅了，请尽快办理借阅手续。',                                                                                                                                  0),
(7,  'SUBSCRIPTION','订阅书籍状态更新',                             '您订阅的《三体》有读者已归还，目前可借数量：2 本。',                                                                                                                                    0),
(8,  'SUBSCRIPTION','订阅书籍可借通知',                             '您订阅的《活着》现已开放借阅，库存充足。',                                                                                                                                                1);

-- ============================================================
-- 8. 公告数据（5 条，4 条已发布 + 1 条草稿）
-- 发布者均为管理员（user_id=1）
-- ============================================================
INSERT IGNORE INTO `announcement` (`title`, `content`, `publisher_id`, `is_published`) VALUES
('BookNexus 图书馆正式开馆通知',    '各位读者：BookNexus 图书管理系统已于2026年5月正式上线运行。本馆藏有各类图书50余册，涵盖文学、科学、技术、历史、哲学等多个领域。欢迎各位读者前来借阅！开放时间：周一至周五 9:00-21:00，周末 10:00-18:00。',                                                                                                                                        1, 1),
('借阅规则与注意事项',              '借阅规则：1. 每位读者最多可同时借阅5本图书；2. 单次借阅期限为30天；3. 到期前7天内可申请续借1次，续借期限为15天；4. 逾期未还将按0.1元/天收取逾期费用；5. 请爱护图书，如有损坏或遗失需照价赔偿。',                                                                                                                                                  1, 1),
('五一假期图书馆开放安排',          '各位读者：根据学校五一假期安排，图书馆将于5月1日至5月3日闭馆，5月4日起恢复正常开放。请各位读者提前安排好借还书时间，避免产生逾期费用。逾期日不计入假期期间。祝大家假期愉快！',                                                                                                        1, 1),
('新书上架：技术类经典书籍推荐',    '本月新上架图书推荐：《代码整洁之道》《Design Patterns》《The Pragmatic Programmer》《Refactoring》《Introduction to Algorithms》等经典技术书籍已入库。欢迎各位同学前来借阅学习！',                                                                                                    1, 1),
('暑期阅读活动策划方案（草稿）',    '暑期将至，图书馆计划举办"书香暑期"主题阅读活动，内容包括：读书分享会、阅读打卡挑战、优秀读后感评选等。活动时间暂定7月-8月，具体安排待定。欢迎各位读者提出建议！',                                                                                                                    1, 0);

-- ============================================================
-- 9. 读者留言/建议（7 条，部分有管理员回复）
-- ============================================================
INSERT IGNORE INTO `message` (`user_id`, `content`, `reply`, `reply_at`, `replier_id`) VALUES
-- 已有管理员回复（4 条）
(2,  '建议图书馆增加更多计算机和编程相关的书籍，特别是人工智能和机器学习方向的新书。',            '感谢您的建议！我们已经注意到这方面的需求，近期正在采购AI和机器学习方向的书籍，预计下月到馆。', DATE_SUB(CURDATE(), INTERVAL 2 DAY), 1),
(3,  '图书馆的WiFi信号在二楼阅览区不太稳定，能否检查一下？',                                      '已收到您的反馈，我们已联系网络管理部门，将在本周内进行信号检测和优化。',                     DATE_SUB(CURDATE(), INTERVAL 1 DAY), 1),
(5,  '希望能延长周末的开放时间，很多同学周末才有时间来看书。',                                      '我们正在评估周末延长开放时间的可行性，会综合考虑读者需求和工作人员安排，尽快给您答复。',   DATE_SUB(CURDATE(), INTERVAL 3 DAY), 1),
(7,  '《三体》系列书籍太受欢迎了，经常借不到，建议多采购几套。',                                    '感谢反馈！《三体》系列确实非常受欢迎，我们已计划增加采购数量。目前已新到一套，欢迎借阅。', DATE_SUB(CURDATE(), INTERVAL 2 DAY), 1),
-- 暂无管理员回复（3 条）
(4,  '自助借还书机偶尔会出现扫描不到条码的情况，希望可以检修一下。',                                NULL, NULL, NULL),
(6,  '建议设置一个"新书推荐"专栏，方便大家了解最新入库的书籍。',                                     NULL, NULL, NULL),
(8,  '电子阅览室的耳机有些已经损坏了，建议更换一批新耳机。',                                        NULL, NULL, NULL);

-- ============================================================
-- 10. 操作日志（18 条，覆盖多种操作类型和 2 条 FAILURE）
-- ============================================================
INSERT IGNORE INTO `operation_log` (`operator`, `action`, `target_type`, `target_id`, `detail`, `ip`, `result`, `created_at`) VALUES
-- 书籍操作（5 条）
('admin', 'CREATE_BOOK',          'book',         6,   '{"title":"三体","author":"刘慈欣","isbn":"978-7-5366-6163-4"}',                          '192.168.1.100', 'SUCCESS', DATE_SUB(CURDATE(), INTERVAL 30 DAY)),
('admin', 'CREATE_BOOK',          'book',         31,  '{"title":"Clean Code","author":"Robert C. Martin"}',                                       '192.168.1.100', 'SUCCESS', DATE_SUB(CURDATE(), INTERVAL 29 DAY)),
('admin', 'UPDATE_BOOK',          'book',         1,   '{"field":"stock","old_value":1,"new_value":5}',                                            '192.168.1.100', 'SUCCESS', DATE_SUB(CURDATE(), INTERVAL 28 DAY)),
('admin', 'CREATE_BOOK',          'book',         42,  '{"title":"1984","author":"George Orwell","isbn":"978-0-451-52493-5"}',                      '192.168.1.100', 'SUCCESS', DATE_SUB(CURDATE(), INTERVAL 22 DAY)),
('admin', 'DELETE_BOOK',          'book',         48,  '{"error":"Cannot delete book with active subscriptions"}',                                 '192.168.1.100', 'FAILURE', DATE_SUB(CURDATE(), INTERVAL 10 DAY)),
-- 用户操作（2 条）
('admin', 'CREATE_USER',          'user',         2,   '{"username":"reader01","role":"USER"}',                                                      '192.168.1.100', 'SUCCESS', DATE_SUB(CURDATE(), INTERVAL 27 DAY)),
('admin', 'DISABLE_USER',         'user',         10,  '{"username":"disabled_user","reason":"测试账号"}',                                            '192.168.1.100', 'SUCCESS', DATE_SUB(CURDATE(), INTERVAL 20 DAY)),
-- 借阅操作（5 条）
('admin', 'APPROVE_BORROW',       'borrow',       3,   '{"book_id":2,"user_id":3,"status":"APPROVED"}',                                             '192.168.1.100', 'SUCCESS', DATE_SUB(CURDATE(), INTERVAL 18 DAY)),
('admin', 'APPROVE_BORROW',       'borrow',       4,   '{"book_id":4,"user_id":4,"status":"APPROVED"}',                                             '192.168.1.100', 'SUCCESS', DATE_SUB(CURDATE(), INTERVAL 17 DAY)),
('admin', 'REJECT_BORROW',        'borrow',       21,  '{"book_id":1,"user_id":3,"reason":"您已有该书的历史借阅记录"}',                              '192.168.1.100', 'SUCCESS', DATE_SUB(CURDATE(), INTERVAL 4 DAY)),
('admin', 'REJECT_BORROW',        'borrow',       22,  '{"book_id":48,"user_id":8,"reason":"该书目前处于损坏状态"}',                               '192.168.1.100', 'SUCCESS', DATE_SUB(CURDATE(), INTERVAL 3 DAY)),
('admin', 'APPROVE_BORROW',       'borrow',       99,  '{"error":"Borrow record not found","attempted_id":99}',                                     '192.168.1.100', 'FAILURE', DATE_SUB(CURDATE(), INTERVAL 6 DAY)),
-- 归还确认（2 条）
('admin', 'CONFIRM_RETURN',       'borrow',       13,  '{"book_id":1,"user_id":3,"fine_amount":0}',                                                 '192.168.1.100', 'SUCCESS', DATE_SUB(CURDATE(), INTERVAL 28 DAY)),
('admin', 'CONFIRM_RETURN',       'borrow',       14,  '{"book_id":1,"user_id":4,"fine_amount":0.50}',                                              '192.168.1.100', 'SUCCESS', DATE_SUB(CURDATE(), INTERVAL 25 DAY)),
-- 公告/通知/留言操作（3 条）
('admin', 'PUBLISH_ANNOUNCEMENT', 'announcement', 1,   '{"title":"BookNexus 图书馆正式开馆通知"}',                                                   '192.168.1.100', 'SUCCESS', DATE_SUB(CURDATE(), INTERVAL 26 DAY)),
('admin', 'SEND_NOTIFICATION',    'notification', 6,   '{"type":"OVERDUE","user_id":5,"title":"逾期还书提醒"}',                                      '192.168.1.100', 'SUCCESS', DATE_SUB(CURDATE(), INTERVAL 8 DAY)),
('admin', 'SEND_NOTIFICATION',    'notification', 7,   '{"type":"OVERDUE","user_id":6,"title":"逾期还书提醒"}',                                      '192.168.1.100', 'SUCCESS', DATE_SUB(CURDATE(), INTERVAL 7 DAY)),
('admin', 'REPLY_MESSAGE',        'message',      1,   '{"user_id":2,"content_preview":"建议图书馆增加更多..."}',                                     '192.168.1.100', 'SUCCESS', DATE_SUB(CURDATE(), INTERVAL 2 DAY));

-- ============================================================
-- 验证查询（执行后检查数据完整性）
-- 每个查询附期望值注释，运行: mysql -u root booknexus < sql/seed-data.sql
-- ============================================================

-- 1. 用户总数：期望 10（1 admin + 9 user）
-- 期望: 10
SELECT 'Q1: user total' AS check_name, COUNT(*) AS actual, '10' AS expected FROM `user`;

-- 2. 普通用户数量：期望 9
-- 期望: 9
SELECT 'Q2: regular users' AS check_name, COUNT(*) AS actual, '9' AS expected FROM `user` WHERE `role` = 'USER';

-- 3. 禁用用户数量：期望 >=1
-- 期望: >=1
SELECT 'Q3: disabled users' AS check_name, COUNT(*) AS actual, '>=1' AS expected FROM `user` WHERE `status` = 'DISABLED';

-- 4. 书籍总数（未删除）：期望 50
-- 期望: 50
SELECT 'Q4: book total' AS check_name, COUNT(*) AS actual, '50' AS expected FROM `book` WHERE `is_deleted` = 0;

-- 5. ISBN 唯一性：期望 50 个不同 ISBN
-- 期望: 50
SELECT 'Q5: unique ISBNs' AS check_name, COUNT(DISTINCT `isbn`) AS actual, '50' AS expected FROM `book`;

-- 6. 损坏/丢失的书籍：期望 >=2
-- 期望: >=2
SELECT 'Q6: damaged/lost books' AS check_name, COUNT(*) AS actual, '>=2' AS expected FROM `book` WHERE `status` IN ('DAMAGED', 'LOST');

-- 7. 借阅状态覆盖：期望 6 种不同状态
-- 期望: 6
SELECT 'Q7: borrow status coverage' AS check_name, COUNT(DISTINCT `status`) AS actual, '6' AS expected FROM `borrow_record`;

-- 8. 逾期记录：期望 >=2（status 为 BORROWED 或 RENEWED 且 due_date < CURDATE()）
-- 期望: >=2
SELECT 'Q8: overdue borrows' AS check_name, COUNT(*) AS actual, '>=2' AS expected FROM `borrow_record` WHERE `status` IN ('BORROWED', 'RENEWED') AND `due_date` < CURDATE();

-- 9. 有罚款的借阅记录：期望 >=1
-- 期望: >=1
SELECT 'Q9: borrows with fine' AS check_name, COUNT(*) AS actual, '>=1' AS expected FROM `borrow_record` WHERE `fine_amount` > 0;

-- 10. 库存一致性（无不一致记录）：期望 0
-- 期望: 0
SELECT 'Q10: stock consistency errors' AS check_name, COUNT(*) AS actual, '0' AS expected
FROM `book` b
LEFT JOIN (
  SELECT `book_id`, COUNT(*) AS borrowed_count
  FROM `borrow_record`
  WHERE `status` IN ('BORROWED', 'RENEWED', 'APPROVED') AND `is_deleted` = 0
  GROUP BY `book_id`
) br ON b.`id` = br.`book_id`
WHERE b.`available_stock` != b.`stock` - COALESCE(br.`borrowed_count`, 0);

-- 11. 外键引用完整性 — 借阅记录用户 ID：期望 0 条无效引用
-- 期望: 0
SELECT 'Q11: orphan borrow user refs' AS check_name, COUNT(*) AS actual, '0' AS expected
FROM `borrow_record` br
LEFT JOIN `user` u ON br.`user_id` = u.`id`
WHERE u.`id` IS NULL;

-- 12. 外键引用完整性 — 借阅记录书籍 ID：期望 0 条无效引用
-- 期望: 0
SELECT 'Q12: orphan borrow book refs' AS check_name, COUNT(*) AS actual, '0' AS expected
FROM `borrow_record` br
LEFT JOIN `book` b ON br.`book_id` = b.`id`
WHERE b.`id` IS NULL;

-- 13. 通知类型覆盖：期望 3 种（SYSTEM / SUBSCRIPTION / OVERDUE）
-- 期望: 3
SELECT 'Q13: notification type coverage' AS check_name, COUNT(DISTINCT `type`) AS actual, '3' AS expected FROM `notification`;

-- 14. 草稿公告数量：期望 >=1
-- 期望: >=1
SELECT 'Q14: draft announcements' AS check_name, COUNT(*) AS actual, '>=1' AS expected FROM `announcement` WHERE `is_published` = 0;

-- 15. 收藏总数：期望 15-20
-- 期望数量仅供参考
SELECT 'Q15: favorite count' AS check_name, COUNT(*) AS actual FROM `favorite`;

-- 16. 已回复留言数量：期望 >=3
-- 期望: >=3
SELECT 'Q16: replied messages' AS check_name, COUNT(*) AS actual, '>=3' AS expected FROM `message` WHERE `reply` IS NOT NULL;

-- 17. 操作日志目标类型覆盖：期望 >=3
-- 期望: >=3
SELECT 'Q17: log target type coverage' AS check_name, COUNT(DISTINCT `target_type`) AS actual, '>=3' AS expected FROM `operation_log`;

-- 18. JSON 字段合法性：期望 0 条非法 JSON
-- 期望: 0
SELECT 'Q18: invalid JSON logs' AS check_name, COUNT(*) AS actual, '0' AS expected FROM `operation_log` WHERE JSON_VALID(`detail`) = 0;

-- ============================================================
-- 验证查询结束
-- 如所有 actual 与 expected 一致，说明数据导入完整正确
-- ============================================================
