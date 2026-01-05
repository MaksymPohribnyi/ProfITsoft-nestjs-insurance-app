interface TestEnv {
  MONGO_IMAGE: string;
  MONGO_DB: string;
  MONGO_PORT: number;
}

const ERROR_MESSAGE = '❌ Missing or invalid test environment variable:';

export const useTestEnv = (): TestEnv => {
  const { MONGO_PORT, MONGO_IMAGE, MONGO_DB } = process.env;

  if (!MONGO_PORT || isNaN(parseInt(MONGO_PORT, 10))) {
    throw new Error(`${ERROR_MESSAGE} MONGO_PORT`);
  }

  if (!MONGO_IMAGE) {
    throw new Error(`${ERROR_MESSAGE} MONGO_IMAGE`);
  }

  if (!MONGO_DB) {
    throw new Error(`${ERROR_MESSAGE} MONGO_DB`);
  }

  return {
    MONGO_IMAGE,
    MONGO_DB,
    MONGO_PORT: parseInt(MONGO_PORT, 10),
  };
};
